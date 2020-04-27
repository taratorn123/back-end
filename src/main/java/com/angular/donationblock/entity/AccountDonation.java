package com.angular.donationblock.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.Date;
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
	public String toString()
	{
		return "ID : "+this.getId()+" From "+this.getUser().getUsername()+
				" To "+this.getCampaign().getCampaignName()+" Amount "+this.getAmount();
	}
//	public Date TimestampToDateExample (){
//    	return Date date=new Date(this.getTimestamp().getTime());
//	}
}
