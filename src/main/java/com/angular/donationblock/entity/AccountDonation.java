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
    private String anonymousFlag;
    
    @ManyToOne(cascade = CascadeType.MERGE)
    private Campaign campaign;

    @ManyToOne(cascade = CascadeType.MERGE)
    private User user;
    
    
    public AccountDonation() 
    {
	}

	public AccountDonation(User user, Campaign campaign, String amount, String comment, String anonymousFlag)
    {
    	this.user = user;
    	this.campaign = campaign;
    	this.amount = amount;
    	this.comment = comment;
    	this.anonymousFlag = anonymousFlag;
    }

	public String getAmount() 
	{
		return amount;
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
	public String getAnonymousFlag() 
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
}
