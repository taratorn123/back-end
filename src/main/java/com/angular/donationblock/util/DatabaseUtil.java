package com.angular.donationblock.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.angular.donationblock.entity.User;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.UserRepository;

@Component
public class DatabaseUtil
{
	@Autowired
	private CampaignRepository campaignRepo;
	@Autowired
	private AccountDonationRepository accountDonationRepo;
	@Autowired
	private UserRepository userRepository;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS");
	
	private Server server = new Server(StellarConfig.stellarServer);
	
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
	@Scheduled(fixedRate = 60*1000)
	public void checkSum() throws NoSuchAlgorithmException
	{
		List<AccountDonation> campaignTransaction;
		List<Campaign> allCampaign = campaignRepo.findAll();
		String prevHashTransaction = null;
		String prevHashStellar = null;
		String prevHashTransactionTmp = null;
		String prevHashStellarTmp = null;
		String stellarMemo = null;
		String[] stringSplit = null;
		try
		{
			/*
			 * Query for all campaign in the system
			 * */
			for(Campaign campaign : allCampaign)
			{
				System.out.println("DatabaseUtil : ****************************************** Campaign name : "+campaign.getCampaignName()+" *************************************************" );

				/*
				 * Get public key from campaign owner
				 * */
				String responseAcc = campaign.getUser().getPublicKey();
				/*
				 * Get all transaction that owned this campaign
				 * */
//				campaignTransaction = accountDonationRepo.findAllByCampaignId(campaign.getId());
				campaignTransaction = accountDonationRepo.findAllByCampaignIdOrderByTimestamp(campaign.getId());
				for(AccountDonation accountDonation : campaignTransaction)
				{
					System.out.println(accountDonation.getId());
					System.out.println(accountDonation.getTimestamp().toString());
				}
				/* Request for payment by campaign owner public key*/
				PaymentsRequestBuilder request = server.payments().forAccount(responseAcc);
				ArrayList<OperationResponse> paymentsRequest = new ArrayList<OperationResponse>();
				/*
				 * This for loop use to count number of recieving payment for each campaign that has been send by our system
				 * */
				for(OperationResponse payment : request.execute().getRecords())
				{
					if(payment instanceof PaymentOperationResponse)
					{
						stellarMemo = server.transactions().transaction(payment.getTransactionHash()).getMemo().toString();
						if(stellarMemo.compareTo("")!=0)
						{
							stringSplit = stellarMemo.split(";");
							if(stringSplit.length == 3)
							{
								if(((PaymentOperationResponse) payment).getTo().compareTo(responseAcc) == 0 && Long.parseLong(stringSplit[0])==campaign.getId())
								{
									
									System.out.println("DatabaseUtil : Campaign ID : "+stringSplit[0]);
									System.out.println("DatabaseUtil : Anonymous : "+stringSplit[1]);
									System.out.println("DatabaseUtil : Exchange Rate : "+stringSplit[2]);
									System.out.println("DatabaseUtil : Transaction Hash "+payment.getTransactionHash());
									System.out.println("DatabaseUtil : Amount "+((PaymentOperationResponse) payment).getAmount());
									paymentsRequest.add(payment);
								}
							}

						}
					}
				}
				System.out.println("DatabaseUtil : Campaign Transaction : "+campaignTransaction.size()+" Stellar Transaction : "+paymentsRequest.size());

				if(campaignTransaction.size() != paymentsRequest.size())
				{
					System.out.println("DatabaseUtil : Transaction size unequal");
					try 
					{
						restoreRemovedTransaction(paymentsRequest,campaignTransaction);
						campaignTransaction = accountDonationRepo.findAllByCampaignIdOrderByTimestamp(campaign.getId());
					} 
					catch (ParseException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(!campaignTransaction.isEmpty() && !paymentsRequest.isEmpty())
				{
					for(int i = 0;i < paymentsRequest.size();i++)
					{
						int check = 0;
						boolean anonymous = false;
						OperationResponse payment = paymentsRequest.get(i);
						stellarMemo = server.transactions().transaction(payment.getTransactionHash()).getMemo().toString();
						stringSplit = stellarMemo.split(";");
						AccountDonation donation = campaignTransaction.get(i);
						System.out.println("DatabaseUtil : ========>> Comparison Checking <<========");
						while(true)
						{
							/*Check Transaction hash*/
							System.out.println("DatabaseUtil : TransactionHash : "+donation.getTransactionHash()+", "+payment.getTransactionHash());
							if(donation.getTransactionHash().compareTo(payment.getTransactionHash()) == 0)
							{
								check++;
							}
							else
							{
								System.out.println("DatabaseUtil : TransactionHash : Conflict");
								donation.setTransactionHash(payment.getTransactionHash());
								accountDonationRepo.save(donation);
							}
							/*Check transaction amount*/
							System.out.println("DatabaseUtil : Amount :"+decimalConverter(donation.getAmount())+", "+decimalConverter(((PaymentOperationResponse) payment).getAmount()));
							if(decimalConverter(donation.getAmount()).compareTo(decimalConverter(((PaymentOperationResponse) payment).getAmount())) == 0)
							{
								check++;
							}
							else
							{
								System.out.println("DatabaseUtil : Amount : Conflict");
								donation.setAmount(decimalConverter(((PaymentOperationResponse) payment).getAmount()));
								accountDonationRepo.save(donation);

							}
							/*Check receiver or beneficiary*/
							System.out.println("DatabaseUtil : Beneficiary Public Key : "+donation.getCampaign().getUser().getPublicKey()+", "+ ((PaymentOperationResponse) payment).getTo());
							if(donation.getCampaign().getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getTo()) == 0)
							{
								check++;
							}
							else
							{
								System.out.println("DatabaseUtil : Beneficiary Public Key : Conflict");
								donation.setCampaign(campaignRepo.findById(Long.parseLong(stringSplit[0])).get());
								accountDonationRepo.save(donation);
							}
							/*Check sender or donor*/
							System.out.println("DatabaseUtil : Donor Public Key : "+donation.getUser().getPublicKey()+", "+ ((PaymentOperationResponse) payment).getFrom());
							if(donation.getUser().getPublicKey().compareTo(((PaymentOperationResponse) payment).getFrom()) == 0)
							{
								check++;
							}
							else
							{
								System.out.println("DatabaseUtil : Donor Public Key : Conflict");
								User user = userRepository.findByPublicKey(((PaymentOperationResponse) payment).getTo());
								donation.setUser(user);
								accountDonationRepo.save(donation);
							}
							/*Check exchange rate*/
							System.out.println("DatabaseUtil : Exchange Rate : "+donation.getExchageRate()+", "+ Double.parseDouble(stringSplit[2]));
							if(donation.getExchageRate() == Double.parseDouble(stringSplit[2]))
							{
								check++;
							}
							else
							{
								System.out.println("DatabaseUtil : Exchange : Conflict");
								donation.setExchangeRate(Double.parseDouble(stringSplit[2]));
								accountDonationRepo.save(donation);
							}
							/*Check Anonymous*/
							System.out.println("DatabaseUtil : Anonymous : "+donation.getAnonymousFlag()+", "+ Boolean.parseBoolean(stringSplit[1]));
							if(donation.getAnonymousFlag() == Boolean.parseBoolean(stringSplit[1]))
							{
								check++;
							}
							else
							{
								donation.setAnonymousFlag(Boolean.parseBoolean(stringSplit[1]));
								accountDonationRepo.save(donation);
							}
							/*Check all data information*/
							if(check == 6)
							{
								System.out.println("DatabaseUtil : ========>> Transaction information All equal");
								break;
							}
							else
							{
								System.out.println("DatabaseUtil : ========>> Transaction information NOT equal");
							}
							/* Reset check*/
							check=0;
						}
//						if(prevHashTransaction == null && prevHashStellar == null)
//						{	
//							prevHashTransaction = transactionHash(donation.getTransactionHash(),
//									decimalConverter(donation.getAmount()),
//									donation.getUser().getPublicKey(),
//									donation.getCampaign().getUser().getPublicKey(),
//									null);
//							prevHashStellar = transactionHash(payment.getTransactionHash(),
//									decimalConverter(((PaymentOperationResponse) payment).getAmount()),
//									((PaymentOperationResponse) payment).getFrom(),
//									((PaymentOperationResponse) payment).getTo(),
//									null);
//						}
//						else
//						{
//							prevHashTransaction = transactionHash(donation.getTransactionHash(),
//									decimalConverter(donation.getAmount()),
//									donation.getUser().getPublicKey(),
//									donation.getCampaign().getUser().getPublicKey(),
//									prevHashTransaction);
//							prevHashStellar = transactionHash(payment.getTransactionHash(),
//									decimalConverter(((PaymentOperationResponse) payment).getAmount()),
//									((PaymentOperationResponse) payment).getFrom(),
//									((PaymentOperationResponse) payment).getTo(),
//									prevHashStellar);
//						}
//						System.out.println("DatabaseUtil : System "+prevHashTransaction+" Stellar "+prevHashStellar);
//						if(prevHashTransaction.compareTo(prevHashStellar) != 0)
//						{
//							System.out.println("DatabaseUtil : Data hash conflicted");
//							restoreTransactionData(donation,payment);
//						}
					}
				}
				System.out.println("DatabaseUtil : *************************************************************************************************************************************************\n");
			}
//			if(prevHashTransaction.compareTo(prevHashStellar) == 0)
//			{
//				System.out.println("DatabaseUtil : Database equal");
//			}	
//			else
//			{
//				System.out.println("DatabaseUtil : Database not equal");
//			}
		}
		catch (TooManyRequestsException | IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch(ErrorResponse e1)
		{
			System.out.println(e1.getBody());
		}
		
	}
	/**
	 * This method use digest transaction detail
	 * */
	private String transactionHash(String transactionHash, String amount, String senderPublicKey, String receiverPublicKey, String prevHash) throws NoSuchAlgorithmException
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
	private void restoreRemovedTransaction(ArrayList<OperationResponse> payments,List<AccountDonation> transactions) throws IOException, ParseException
	{
		int counter = 0;
		boolean found = false;
		if(payments.size() < transactions.size())
		{
			for(AccountDonation transaction : transactions)
			{
				for(OperationResponse payment : payments)
				{
					if(payment.getTransactionHash() == transaction.getTransactionHash())
					{
						found = true;
//						payments.remove(payment);
					}
				}
				if(!found)
				{
					System.out.println("DatabaseUtil : Remove transaction ID : "+transaction.getId());
					accountDonationRepo.delete(transaction);
				}
				found = false;
			}
		}
		else
		{
			for(OperationResponse payment : payments)
			{
				if(transactions.size() > 0)
				{
					for(AccountDonation transaction : transactions)
					{
						System.out.println("DatabaseUtil : Comparing transaction in database restoring process "+payment.getTransactionHash()+" "+transaction.getTransactionHash());
						if(payment.getTransactionHash().compareTo(transaction.getTransactionHash()) == 0)
						{
							found = true;
							System.out.println("DatabaseUtil : Transaction "+transaction.getId()+" found");
//							transactions.remove(transaction);
						}
					}
				}
				if(!found)
				{
					String stellarMemo = server.transactions().transaction(payment.getTransactionHash()).getMemo().toString();
					String[] information = stellarMemo.split(";");
					/* Restore information from stellar ledger*/
					User donor = userRepository.findByPublicKey(((PaymentOperationResponse) payment).getFrom());
					Campaign campaign = campaignRepo.findById(Long.parseLong(information[0])).get();
					boolean anonymousFlag = Boolean.parseBoolean(information[1]);
					double exchangeRate = Double.parseDouble(information[2]);
					
					String[] tmpSplit1 = payment.getCreatedAt().split("T");
					String[] tmpSplit2 = tmpSplit1[1].split("Z");
					String date = tmpSplit1[0]+" "+tmpSplit2[0]+".000000";
					String amount = ((PaymentOperationResponse) payment).getAmount();
					String transactionHash = payment.getTransactionHash();
					Date parsedDate = dateFormat.parse(date);
					Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
					System.out.println("DatabaseUtil : Adding Transaction for campaign "+campaign.getId());
					accountDonationRepo.save(new AccountDonation(donor,campaign,amount,anonymousFlag,
							exchangeRate,transactionHash,timestamp));
				}
				found = false;
			}
		}
	}
	
	/**
	 * This method use restore data when system data conlict with stellar data
	 * */
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
				System.out.println("DatabaseUtil : Restoring transaction " +campaignTransaction.getId());
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
