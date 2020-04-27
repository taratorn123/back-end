package com.angular.donationblock.form;


public class AccountDonationForm 
{
	private String campaignId;
	private String userId;
	private String amount;
	private String comment;
	private String privateKey;
	private boolean anonymousFlag;
	private double exchageRate;

	public AccountDonationForm(String campaignId, String userId,String amount,String comment,
			String privateKey, boolean anonymousFlag,double exchangeRate)
	{
		this.campaignId = campaignId;
		this.userId = userId;
		this.amount = amount;
		this.comment=comment;
		this.anonymousFlag=anonymousFlag;
		this.privateKey=privateKey;
		this.exchageRate = exchangeRate;
	}
	public double getExchageRate() 
	{
		return exchageRate;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAmount() 
	{
		return amount;
	}
	public String getComment() 
	{
		return comment;
	}
	public boolean getAnonymousFlag() 
	{
		return anonymousFlag;
	}
	public String getPrivateKey() 
	{
		return privateKey;
	}
	
}
