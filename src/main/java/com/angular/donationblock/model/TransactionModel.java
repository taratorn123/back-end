package com.angular.donationblock.model;
import java.sql.Timestamp;

public class TransactionModel
{
	private long transactionId;
	private String campaignName;
	private String campaignPublicKey;
	private Timestamp publishTime;
	private String userName;
	private String amount;
	private String transactionHash;
	private String baht;
	
	public long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}
	public TransactionModel()
	{
		
	}
	public TransactionModel(long  transactionId,
	String campaignName,
	Timestamp publishTime,
	String userName,
	String amount,
	String campaignPublicKey,
	String transactionHash)
	{
		this.transactionId = transactionId;
		this.campaignName = campaignName;
		this.amount = amount;
		this.transactionHash = transactionHash;
		this.campaignPublicKey = campaignPublicKey;
		this.publishTime = publishTime;
		this.userName = userName;
	}
	public TransactionModel(String campaignName,
			String campaignPublicKey)
	{
		this.campaignName = campaignName;
		this.campaignPublicKey = campaignPublicKey;
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
