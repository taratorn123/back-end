package com.angular.donationblock.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Report extends BaseEntity
{
	@ManyToOne(cascade = CascadeType.MERGE)
	private Campaign campaign;
	@ManyToOne(cascade = CascadeType.MERGE)
    private User user;
	private Timestamp timestamp;
	private String detail;
	
	public Report()
	{	
	}
	public Report(User user, Campaign campaign,String detail)
	{
		this.campaign = campaign;
		this.user = user;
		this.detail = detail;
		Date date = new Date();
    	Long time = date.getTime();
    	this.timestamp = new Timestamp(time);
	}
	public Campaign getCampaign() 
	{
		return campaign;
	}
	public User getUser() 
	{
		return user;
	}
	public Timestamp getTimestamp() 
	{
		return timestamp;
	}
	public String getDetail() 
	{
		return detail;
	}
	

}
