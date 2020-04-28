package com.angular.donationblock.controller;

import com.angular.donationblock.form.AccountDonationForm;
import com.angular.donationblock.model.TransactionModel;
import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.UserRepository;
import com.angular.donationblock.util.AES;
import com.angular.donationblock.util.DatabaseUtil;
import com.angular.donationblock.util.GeneratePdfReport;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TooManyRequestsException;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController

/* Cross-Origin Resource Sharing (CORS) is a security concept that allows 
 * restricting the resources implemented in web browsers. 
 * It prevents the JavaScript code producing or consuming the requests against different origin.*/
@CrossOrigin
public class TransactionController 
{
    @Autowired
    private AccountDonationRepository accountDonationRepository;
    @Autowired 
    private CampaignRepository campaignRepository;
    @Autowired 
    private UserRepository userRepository;
    private Server server = new Server(StellarConfig.stellarServer);
    static Map<String, String> requestParams = new HashMap<>();
    
    @PostMapping("/sendDonation")
    public int addToStellar(@RequestBody AccountDonationForm accountDonationForm) throws IOException 
    {
    	Transaction transaction;
    	KeyPair sourceKey;
    	SubmitTransactionResponse response = null;
    	String hash = null;
    	System.out.println(accountDonationForm.getAmount());
    	System.out.println(accountDonationForm.getCampaignId());
    	System.out.println(accountDonationForm.getUserId());
    	System.out.println(accountDonationForm.getExchageRate());
    	AccountDonation accountDonation = new AccountDonation(
    			userRepository.findByUsername(accountDonationForm.getUserId()),
    			campaignRepository.findById(Long.parseLong(accountDonationForm.getCampaignId())).get(),
    			DatabaseUtil.decimalConverter(accountDonationForm.getAmount()),accountDonationForm.getComment(),accountDonationForm.getAnonymousFlag(),accountDonationForm.getExchageRate());
    	AccountResponse account = server.accounts().account(accountDonation.getUser().getPublicKey());
		System.out.println("Hello "+ account.getAccountId());
		System.out.println("Balances for account " + account.getAccountId());
		/* Check if user have enough balance to donate money, if not return 0*/
		for (AccountResponse.Balance balance : account.getBalances()) 
		{
			if(balance.getAssetType().compareTo("native")==0)
			{
				if(Double.parseDouble(balance.getBalance())-1 < Double.parseDouble(accountDonation.getAmount()))
				{
					System.out.println("Cannot do transaction");
					return 0;
				}
			}
		}
		System.out.println("Get Private key");
    	try
    	{
    		sourceKey = KeyPair.fromSecretSeed(accountDonationForm.getPrivateKey());
    	}
    	catch(Exception e)
    	{
    		return 1;
    	}
    	System.out.println("Building Transaction");
    	if(accountDonationForm.getComment() == null)
    	{
    		transaction = new Transaction.Builder(account,Network.TESTNET)
    		        .addOperation(new PaymentOperation.Builder(accountDonation.getCampaign().getUser().getPublicKey(), new AssetTypeNative(), accountDonation.getAmount()).build())
    		        // Wait a maximum of three minutes for the transaction
    		        .setTimeout(180)
    		        .setOperationFee(100)
    		        .build();
    		// Sign the transaction to prove you are actually the person sending it.
    		System.out.println("Signing");
    		transaction.sign(sourceKey);
    	}
    	else
    	{
    		transaction = new Transaction.Builder(account,Network.TESTNET)
    		        .addOperation(new PaymentOperation.Builder(accountDonation.getCampaign().getUser().getPublicKey(), new AssetTypeNative(), accountDonation.getAmount()).build())
    		        // A memo allows you to add your own metadata to a transaction. It's
    		        // optional and does not affect how Stellar treats the transaction.
    		        .addMemo(Memo.text(accountDonation.getComment()))
    		        // Wait a maximum of three minutes for the transaction
    		        .setTimeout(180)
    		        .setOperationFee(100)
    		        .build();
    		// Sign the transaction to prove you are actually the person sending it.
    		System.out.println("Signing");
    		transaction.sign(sourceKey);
    	}
    	try 
		{
			System.out.println("Get response from serve");
			for(int i = 0;i < 10;i++)
			{
				response = server.submitTransaction(transaction);
				if(response.isSuccess())
				{
					break;
				}
				if(i == 9)
				{
					return 2;
				}
			}
			/* If transaction failed DecodedTransactionResult will return option.abest() instead of json*/
			System.out.println("Get Hash");
			hash = response.getHash();
			System.out.println(response.getDecodedTransactionResult());
			accountDonation.setTransactionHash(hash);
			this.saveTransaction(accountDonation);
			System.out.println("Save database");
			return 3;
		} 
		catch (Exception e) 
		{
			System.out.println("Something went wrong!");
			System.out.println(e.getMessage());
			// If the result is unknown (no response body, timeout etc.) we simply resubmit
			// already built transaction:
			// SubmitTransactionResponse response = server.submitTransaction(transaction);
			return 2;
		}
    }
    private void saveTransaction(AccountDonation accountDonation)
    {
    	accountDonationRepository.save(accountDonation);
    }
    /**
     * This method use to query out all transaction that has been sent to the 
     * selected campaign and return as TransactionForm
     * */
    
    @GetMapping("/getHistoryTransactionCampaign/{campaignId}")
    public List<TransactionModel> getHistoryTransactionCampaign(@PathVariable Long campaignId)
    {
    	Campaign campaign = campaignRepository.findById(campaignId).get();
    	List<TransactionModel> transactionHistory = new ArrayList<TransactionModel>();
    	List<AccountDonation> systemTransaction = accountDonationRepository.findAllByCampaignId(campaignId);
    	System.out.println(systemTransaction.size());
    	if(systemTransaction.isEmpty())
    	{
    		transactionHistory.add(new TransactionModel(campaign.getCampaignName(),campaign.getUser().getPublicKey()));
    		return transactionHistory;
    	}
    	System.out.println("Get history Transaction");
    	Server server = new Server(StellarConfig.stellarServer);
    	String responseAcc = campaign.getUser().getPublicKey();
    	System.out.println("Campaign owner public key : "+responseAcc);
    	PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(responseAcc);
		try 
		{
			ArrayList<OperationResponse> payments = paymentsRequest.execute().getRecords();
			System.out.println("getHistoryTransactionCampaign stellar trasnaction number: "+payments.size());

			for(OperationResponse payment : payments)
			{
				if (payment instanceof PaymentOperationResponse) 
				{
					
					User user = userRepository.findByPublicKey(((PaymentOperationResponse) payment).getFrom());
					if(user != null)
					{
						for(AccountDonation transaction : systemTransaction)
						{
							System.out.println(transaction.getTransactionHash()+" Stellar : "+payment.getTransactionHash());
							if(transaction.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
							{
								System.out.println(transaction.getTransactionHash()+"\n"+payment.getTransactionHash());
								if(transaction.getAnonymousFlag() == true)
								{
									transactionHistory.add(new TransactionModel(transaction.getId(),
											campaign.getCampaignName(),
											transaction.getTimestamp(),
											user.getPublicKey(),
											transaction.getAmount(),
											campaign.getUser().getPublicKey(),
											transaction.getTransactionHash()));
								}
								else
								{
									transactionHistory.add(new TransactionModel(transaction.getId(),
											campaign.getCampaignName(),
											transaction.getTimestamp(),
											user.getUsername(),
											transaction.getAmount(),
											campaign.getUser().getPublicKey(),
											transaction.getTransactionHash()));
								}
								systemTransaction.remove(transaction);
								break;
							}					
						}
					}
				}
			}
			for(TransactionModel transaction : transactionHistory)
			{
				System.out.println(transaction.toString());
			}
		}
		catch (TooManyRequestsException | IOException e1) 
		{
			// TODO Auto-generated catch b0lock
			e1.printStackTrace();
		} 
		catch (ErrorResponse e2)
		{
			System.out.println("Body "+e2.getBody());
			System.out.println("Message "+e2.getMessage());
			System.out.println("GetCause "+e2.getCause());
			System.out.println("To string "+e2.toString());
		}
		return (List<TransactionModel>) transactionHistory;
    }
    
    @GetMapping("/getHistoryTransactionUser/{userId}")
    public List<TransactionModel> getUserTransaction(@PathVariable Long userId)
    {
    	User user = userRepository.findById(userId).get();
    	List<TransactionModel> transactionHistory = new ArrayList<TransactionModel>();
    	List<AccountDonation> systemTransaction = accountDonationRepository.findAllByUserId(userId);
    	if(systemTransaction.isEmpty())
    	{
    		transactionHistory.add(new TransactionModel(user.getFirstName()+" "+user.getLastName(),user.getPublicKey()));
    		return transactionHistory;
    	}
    	Server server = new Server(StellarConfig.stellarServer);
    	String responseAcc = user.getPublicKey();
    	System.out.println("User public key : "+responseAcc);
    	PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(responseAcc);
		try 
		{
			for(OperationResponse payment : paymentsRequest.execute().getRecords())
			{
				if (payment instanceof PaymentOperationResponse) 
				{
					for(AccountDonation transaction : systemTransaction)
					{
						if(transaction.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
						{
							transactionHistory.add(new TransactionModel(transaction.getId(),
									transaction.getCampaign().getCampaignName(),
									transaction.getTimestamp(),
									user.getFirstName()+" "+user.getLastName(),
									transaction.getAmount(),
									user.getPublicKey(),
									transaction.getTransactionHash()));
							System.out.println(transaction.getTimestamp());
							systemTransaction.remove(transaction);
							break;
						}
					}
				}
			}
			for(TransactionModel transaction : transactionHistory)
			{
				System.out.println(transaction.toString());
			}
		}
		catch (TooManyRequestsException | IOException e1) 
		{
			// TODO Auto-generated catch b0lock
			e1.printStackTrace();
		} 
		return (List<TransactionModel>) transactionHistory;
    }
    
    @SuppressWarnings("deprecation")
	@GetMapping("RequestForTransactionReport/{transactionID}")
    public String getAccesstoTransactionReport(@PathVariable String transactionID) throws UnsupportedEncodingException
    {
    	Base32 base32 = new Base32();
    	transactionID = transactionID+";!@#dsFdfgjklcb151981";
    	String output = base32.encodeAsString(transactionID.getBytes());
    	System.out.println("TransactionController : Sending encoded url");
    	return output;
    }
    
    @GetMapping("/getTrasnactionReport/{encoded}")
    public ResponseEntity<InputStreamResource> getReport(@PathVariable String encoded) throws UnsupportedEncodingException
    {
    	try
    	{
    		Base32 base32 = new Base32();
        	String decoded = new String(base32.decode(encoded));
        	String[] splitter = decoded.split(";");
        	String realTransactionId = splitter[0];
        	System.out.println(encoded);
    		long transactionID = Long.parseLong(realTransactionId);
    		AccountDonation transaction = accountDonationRepository.findById(transactionID).get();
        	ByteArrayInputStream reading = GeneratePdfReport.test(transaction);
        	HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=Transaction_Report_"+transactionID+".pdf");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(reading));
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }	
}
