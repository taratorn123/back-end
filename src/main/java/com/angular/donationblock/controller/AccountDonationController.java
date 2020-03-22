package com.angular.donationblock.controller;

import com.angular.donationblock.form.AccountDonationForm;
import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.repository.AccountDonationRepository;
import com.angular.donationblock.repository.CampaignRepository;
import com.angular.donationblock.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import java.io.IOException;

@RestController

/* Cross-Origin Resource Sharing (CORS) is a security concept that allows 
 * restricting the resources implemented in web browsers. 
 * It prevents the JavaScript code producing or consuming the requests against different origin.*/
@CrossOrigin
public class AccountDonationController 
{
    @Autowired
    private AccountDonationRepository accountDonationRepository;
    
    @Autowired CampaignRepository campaignRepository;
    @Autowired UserRepository userRepository;
    private Server server = new Server(StellarConfig.stellarServer);

    @PostMapping("/sendDonation")
    public int addToStellar(@RequestBody AccountDonationForm accountDonationForm) throws IOException 
    {
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
		Transaction transaction = new Transaction.Builder(account,Network.TESTNET)
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
//			System.out.println(response.getExtras());
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

}
