package com.angular.donationblock.model;
import java.sql.Timestamp;

public class TransactionModel
{
	private String campaignName;
	private String campaignPublicKey;
	private Timestamp publishTime;
	private String userName;
	private String amount;
	private String transactionHash;
	
	public TransactionModel()
	{
		
	}
	public TransactionModel(String campaignName,
	Timestamp publishTime,
	String userName,
	String amount,
	String campaignPublicKey,
	String transactionHash)
	{
		this.campaignName = campaignName;
		this.amount = amount;
		this.transactionHash = transactionHash;
		this.campaignPublicKey = campaignPublicKey;
		this.publishTime = publishTime;
		this.userName = userName;
	}
	public String getCampaignName() 
	{
		return campaignName;
	}
	public String getCampaignPublicKey() 
	{
		return campaignPublicKey;
	}
	public Timestamp getPublishTime() 
	{
		return publishTime;
	}
	public String getUserName() 
	{
		return userName;
	}
	public String getAmount() 
	{
		return amount;
	}
	public String getTransactionHash() 
	{
		return transactionHash;
	}
	public String toString()
	{
		return this.campaignName+" amount "+this.amount+" by "+this.userName;
	}
}
