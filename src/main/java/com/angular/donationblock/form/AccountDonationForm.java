package com.angular.donationblock.form;

public class AccountDonationForm 
{
	private String amount;
	private String comment;
	private String anonymousFlag;
	private String destination;
	private String publicKey;
	private String privateKey;
	public AccountDonationForm(String amount,String comment,
			String anonymousFlag,String destination,String publicKey,String privateKey)
	{
		this.amount = amount;
		this.comment=comment;
		this.anonymousFlag=anonymousFlag;
		this.destination=destination;
		this.publicKey=publicKey;
		this.privateKey=privateKey;
	}
	public String getAmount() 
	{
		return amount;
	}
	public String getComment() 
	{
		return comment;
	}
	public String getAnonymousFlag() 
	{
		return anonymousFlag;
	}
	public String getDestination() 
	{
		return destination;
	}
	public String getPublicKey() 
	{
		return publicKey;
	}
	public String getPrivateKey() 
	{
		return privateKey;
	}
	
}
