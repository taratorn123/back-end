package com.angular.donationblock.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.Date;

@Entity
public class AccountDonation extends BaseEntity
{
    private String amount;
    private Timestamp timestamp;
    private String comment;
    private String transactionHash;
    private String anonymousFlag;
    
    @ManyToOne
    private Campaign campaign;

    @ManyToOne
    private User user;
    
    public AccountDonation()
    {
    	
    }
    public AccountDonation(String transactionHash,String amount,String comment,String anonymousFlag)
    {
    	this.amount = amount;
    	this.comment = comment;
    	this.anonymousFlag = anonymousFlag;
    	Date date = new Date();
    	Long time = date.getTime();
    	this.timestamp = new Timestamp(time);
    	this.transactionHash = transactionHash;
    }

	public String getAmount() 
	{
		return amount;
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
