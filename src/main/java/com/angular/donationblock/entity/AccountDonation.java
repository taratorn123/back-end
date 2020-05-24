package com.angular.donationblock.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Entity
public class AccountDonation extends BaseEntity
{
    private String amount;
    private Timestamp timestamp;
    private String comment;
    private String transactionHash;
    private boolean anonymousFlag;
    private double exchangeRate;
    
    @ManyToOne(cascade = CascadeType.MERGE)
    private Campaign campaign;

    @ManyToOne(cascade = CascadeType.MERGE)
    private User user;
    
    
	public AccountDonation() 
    {
    	
	}

	public AccountDonation(User user, Campaign campaign, String amount, String comment, boolean anonymousFlag,double exchangeRate)
    {
    	this.user = user;
    	this.campaign = campaign;
    	this.amount = amount;
    	this.comment = comment;
    	this.anonymousFlag = anonymousFlag;
    	this.exchangeRate = exchangeRate;
    }
	public AccountDonation(User user, Campaign campaign, String amount, 
			String comment, boolean anonymousFlag,double exchangeRate, String transactionHash)
    {
    	this.user = user;
    	this.campaign = campaign;
    	this.amount = amount;
    	this.comment = comment;
    	this.anonymousFlag = anonymousFlag;
    	this.exchangeRate = exchangeRate;
    	this.transactionHash = transactionHash;
    	this.setDeleted(false);
    }

	public AccountDonation(User user, Campaign campaign, String amount, 
			boolean anonymousFlag, double exchangeRate, String transactionHash,
			Timestamp timestamp)
	{
		this.user= user;
		this.campaign = campaign;
		this.amount = amount;
		this.anonymousFlag = anonymousFlag;
		this.exchangeRate = exchangeRate;
		this.transactionHash = transactionHash;
		this.timestamp = timestamp;
		
	}
	public double getExchageRate() 
    {
		return exchangeRate;
	}

	public String getAmount() 
	{
		return amount;
	}
	public void setAmount(String amount)
	{
		this.amount = amount;
	}
	public void setTransactionHash(String transactionHash) 
	{
		Date date = new Date();
    	Long time = date.getTime();
    	this.timestamp = new Timestamp(time);
		this.transactionHash = transactionHash;
	}
	public Timestamp getTimestamp() 
	{
		return timestamp;
	}
	public String getComment() 
	{
		return comment;
	}
	public String getTransactionHash() 
	{
		return transactionHash;
	}
	public boolean getAnonymousFlag() 
	{
		return anonymousFlag;
	}
	public Campaign getCampaign() 
	{
		return campaign;
	}
	public User getUser() 
	{
		return user;
	}
	public void setUser(User user)
	{
		this.user = user;
	}
	public void setCampaign(Campaign campaign)
	{
		this.campaign = campaign;
	}
	public double getExchangeRate() 
	{
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) 
	{
		this.exchangeRate = exchangeRate;
	}

	public String toString()
	{
		return "ID : "+this.getId()+" From "+this.getUser().getUsername()+
				" To "+this.getCampaign().getCampaignName()+" Amount "+this.getAmount();
	}
	public void setAnonymousFlag(boolean anonymousFlag)
	{
		this.anonymousFlag = anonymousFlag;
	}
//	@GetMapping("getTotalDonate/{campaignId}")
//    public String getTotalDonate(@PathVariable long campaignId)
//    {
//    	double totalDonate = 0;
//    	List<AccountDonation> transactions = accountDonationRepository.findAllByCampaignId(campaignId);
//    	
//    	for(AccountDonation transaction : transactions)
//    	{
//    		totalDonate += transaction.getExchageRate()*Double.parseDouble(transaction.getAmount());
//    	}
//    	DecimalFormat df = new DecimalFormat("#.00"); 
//    	return df.format(totalDonate);
//    }
}
