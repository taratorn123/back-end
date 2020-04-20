package com.angular.donationblock.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TooManyRequestsException;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;

@Component
public class DatabaseUtil
{
	@Autowired
	private CampaignRepository campaignRepo;
	@Autowired
	private AccountDonationRepository accountDonationRepo;
	
	private byte[] getSHA(String input) throws NoSuchAlgorithmException 
    {  
        // Static getInstance method is called with hashing SHA  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        // digest() method called  
        // to calculate message digest of an input  
        // and return array of byte 
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    } 
    
	private String toHexString(byte[] hash) 
    { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    } 
	public static String decimalConverter(String number)
	{
		String output = number;
		double tmp = Double.parseDouble(number);
		DecimalFormat format = new DecimalFormat("0.00"); 		
		return format.format(tmp);
	}
	//@Scheduled(fixedRate = 60*1000)
	public void checkSum() throws NoSuchAlgorithmException
	{
		List<AccountDonation> campaignTransaction;
		List<Campaign> allCampaign = campaignRepo.findAll();
		String prevHashTransaction = null;
		String prevHashStellar = null;
		Server server = new Server(StellarConfig.stellarServer);
		try
		{
			for(Campaign campaign : allCampaign)
			{
				System.out.println("*************************************************************************************************************************************************\n");

				System.out.println("******************************************************** Campaign name : "+campaign.getCampaignName()+" *********************************************************" );
				if(campaign.getUser() == null)
				{
					System.out.println("*************************************************************************************************************************************************\n");
					continue;
				}
				String responseAcc = campaign.getUser().getPublicKey();
				campaignTransaction = accountDonationRepo.findAllByCampaignId(campaign.getId());
				PaymentsRequestBuilder request = server.payments().forAccount(responseAcc);
				ArrayList<OperationResponse> paymentsRequest = new ArrayList<OperationResponse>();
				for(OperationResponse payment : request.execute().getRecords())
				{
					if(payment instanceof PaymentOperationResponse)
					{
						paymentsRequest.add(payment);
					}
				}
				if(campaignTransaction.size() != paymentsRequest.size())
				{
					System.out.println("Campaign Transaction : "+campaignTransaction.size()+" Stellar Transaction : "+paymentsRequest.size());
					System.out.println("Unequal");
				}
				if(!campaignTransaction.isEmpty())
				{
					for(int i = 0;i < paymentsRequest.size();i++)
					{
						
						OperationResponse payment = paymentsRequest.get(i);
						AccountDonation donation = campaignTransaction.get(i);
						System.out.println("========>> Comparison Checking <<========");
						if(donation.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
						{
							System.out.println("Transaction Hash");
							if(decimalConverter(donation.getAmount()).compareTo(decimalConverter(((PaymentOperationResponse) payment).getAmount())) == 0)
							{
								System.out.println("Amount");
								if(donation.getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getFrom()) == 0)
								{
									System.out.println("Public Key");
									if(donation.getCampaign().getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getTo()) == 0)
									{
										System.out.println("transactionHash : "+donation.getTransactionHash()+", "+payment.getTransactionHash()+"\n"
												+"amount :"+decimalConverter(donation.getAmount())+", "+decimalConverter(((PaymentOperationResponse) payment).getAmount())+"\n"
												+"Donor :"+donation.getUser().getPublicKey()+", "+((PaymentOperationResponse) payment).getFrom()+"\n"
												+"Receiver :"+donation.getCampaign().getUser().getPublicKey()+", "+((PaymentOperationResponse) payment).getTo());
										System.out.println("========>> All equal");
										
									}
								}
							}							
						}
						
						if(prevHashTransaction == null && prevHashStellar == null)
						{	
							System.out.println("Hash null");
							prevHashTransaction = transactionHash(donation.getTransactionHash(),
									decimalConverter(donation.getAmount()),
									donation.getUser().getPublicKey(),
									donation.getCampaign().getUser().getPublicKey(),
									null);
							prevHashStellar = transactionHash(payment.getTransactionHash(),
									decimalConverter(((PaymentOperationResponse) payment).getAmount()),
									((PaymentOperationResponse) payment).getFrom(),
									((PaymentOperationResponse) payment).getTo(),
									null);
						}
						else
						{
							prevHashTransaction = transactionHash(donation.getTransactionHash(),
									decimalConverter(donation.getAmount()),
									donation.getUser().getPublicKey(),
									donation.getCampaign().getUser().getPublicKey(),
									prevHashTransaction);
							prevHashStellar = transactionHash(payment.getTransactionHash(),
									decimalConverter(((PaymentOperationResponse) payment).getAmount()),
									((PaymentOperationResponse) payment).getFrom(),
									((PaymentOperationResponse) payment).getTo(),
									prevHashStellar);
						}
						System.out.println("System "+prevHashTransaction+" Stellar "+prevHashStellar);
						if(prevHashTransaction.compareTo(prevHashStellar) != 0)
						{
							System.out.println("Hash conflicted");
							restoreTransactionData(donation,payment);
						}
					}
				}
				System.out.println("*************************************************************************************************************************************************\n");
			}
			if(prevHashTransaction.compareTo(prevHashStellar) == 0)
			{
				System.out.println("Database equal");
			}	
			else
			{
				System.out.println("Database not equal");
				//restoreDatabase();
			}
		}
		catch (TooManyRequestsException | NoSuchAlgorithmException | IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch(ErrorResponse e1)
		{
			System.out.println(e1.getBody());
		}
		
	}
	public String transactionHash(String transactionHash, String amount, String senderPublicKey, String receiverPublicKey, String prevHash) throws NoSuchAlgorithmException
	{
		String hashTransaction = toHexString(getSHA(transactionHash));
		String hashAmount = toHexString(getSHA(amount));
		String hashSender = toHexString(getSHA(senderPublicKey));
		String hashReceiver = toHexString(getSHA(receiverPublicKey));
		if(prevHash == null)
		{
			return toHexString(getSHA(hashTransaction+hashAmount+hashSender+hashReceiver));
		}
		return toHexString(getSHA(prevHash+hashTransaction+hashAmount+hashSender+hashReceiver));
	}
	private void restoreTransactionData(AccountDonation campaignTransaction, OperationResponse payment)
	{
		if(campaignTransaction.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
		{
			System.out.println("Transaction Hash");
			if(decimalConverter(campaignTransaction.getAmount()).compareTo(decimalConverter(((PaymentOperationResponse) payment).getAmount())) == 0)
			{
				System.out.println("Amount");
				if(campaignTransaction.getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getFrom()) == 0)
				{
					System.out.println("Public Key");
					if(campaignTransaction.getCampaign().getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getTo()) == 0)
					{
						System.out.println("========>> All equal");
					}
					else
					{
						System.out.println("Conflicted receiver public key");
					}
				}
				else
				{
					System.out.println("Conflicted sender public key");
				}
			}
			else
			{
				campaignTransaction.setAmount(decimalConverter(((PaymentOperationResponse) payment).getAmount()));
			}
		}
		else
		{
			campaignTransaction.setTransactionHash(payment.getTransactionHash());
		}
		accountDonationRepo.save(campaignTransaction);
		
	}
}
