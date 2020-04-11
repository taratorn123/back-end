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
import com.angular.donationblock.util.GeneratePdfReport;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.ArrayList;
import java.util.List;

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

    @PostMapping("/sendDonation")
    public int addToStellar(@RequestBody AccountDonationForm accountDonationForm) throws IOException 
    {
    	Transaction transaction;
    	System.out.println(accountDonationForm.getAmount());
    	System.out.println(accountDonationForm.getCampaignId());
    	System.out.println(accountDonationForm.getUserId());
    	AccountDonation accountDonation = new AccountDonation(
    			userRepository.findByUsername(accountDonationForm.getUserId()),
    			campaignRepository.findById(Long.parseLong(accountDonationForm.getCampaignId())).get(),
    			accountDonationForm.getAmount(),accountDonationForm.getComment(),accountDonationForm.getAnonymousFlag());
    	AccountResponse account = server.accounts().account(accountDonation.getUser().getPublicKey());
		System.out.println("Hello "+ account.getAccountId());
		System.out.println("Balances for account " + account.getAccountId());
		/* Check if user have enough balance to donate money, if not return 0*/
		for (AccountResponse.Balance balance : account.getBalances()) 
		{
			System.out.println("Im here 1");
			if(balance.getAssetType().compareTo("native")==0)
			{
				System.out.println("Im here 2");
				if(Double.parseDouble(balance.getBalance())-1 < Double.parseDouble(accountDonation.getAmount()))
				{
					System.out.println("Cannot do transaction");
					return 0;
				}
			}
		}
		
		System.out.println("Get Private key");
    	KeyPair sourceKey = KeyPair.fromSecretSeed(accountDonationForm.getPrivateKey());
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
			SubmitTransactionResponse response = server.submitTransaction(transaction);
			System.out.println("Get Hash");
			String hash = response.getHash();
			if(!response.isSuccess())
			{
				return 1;
			}
			/* If transaction failed DecodedTransactionResult will return option.abest() instead of json*/
			System.out.println(response.getDecodedTransactionResult());
			accountDonation.setTransactionHash(hash);
			this.saveTransaction(accountDonation);
			System.out.println("Save database");
			return 2;
		} 
		catch (Exception e) 
		{
			System.out.println("Something went wrong!");
			System.out.println(e.getMessage());
			// If the result is unknown (no response body, timeout etc.) we simply resubmit
			// already built transaction:
			// SubmitTransactionResponse response = server.submitTransaction(transaction);
		}
        return 3;
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
    	List<TransactionModel> transactionHistory = new ArrayList<TransactionModel>();
    	List<AccountDonation> systemTransaction = accountDonationRepository.findAllByCampaignId(campaignId);
    	if(systemTransaction.isEmpty())
    	{
    		return null;
    	}
    	Campaign campaign = campaignRepository.findById(campaignId).get();
    	System.out.println("Get history Transaction");
    	Server server = new Server(StellarConfig.stellarServer);
    	String responseAcc = campaign.getUser().getPublicKey();
    	System.out.println("Campaign owner public key : "+responseAcc);
    	PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(responseAcc);
		try 
		{
			for(OperationResponse payment : paymentsRequest.execute().getRecords())
			{
				if (payment instanceof PaymentOperationResponse) 
				{
					
					User user = userRepository.findByPublicKey(((PaymentOperationResponse) payment).getFrom());
					if(user != null)
					{
						for(AccountDonation transaction : systemTransaction)
						{
							if(transaction.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
							{
								System.out.println(transaction.getTransactionHash()+"\n"+payment.getTransactionHash());
								if(transaction.getAnonymousFlag() == true)
								{
									transactionHistory.add(new TransactionModel(campaign.getCampaignName(),
											transaction.getTimestamp(),
											user.getPublicKey(),
											transaction.getAmount(),
											campaign.getUser().getPublicKey(),
											transaction.getTransactionHash()));
								}
								else
								{
									transactionHistory.add(new TransactionModel(campaign.getCampaignName(),
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
    		return null;
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
							transactionHistory.add(new TransactionModel(transaction.getCampaign().getCampaignName(),
									transaction.getTimestamp(),
									user.getPublicKey(),
									transaction.getAmount(),
									transaction.getCampaign().getUser().getPublicKey(),
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
    @GetMapping("/getTrasnactionReport/{transactionID}")
    public ResponseEntity<InputStreamResource> getReport(@PathVariable long transactionID)
    {
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
}
