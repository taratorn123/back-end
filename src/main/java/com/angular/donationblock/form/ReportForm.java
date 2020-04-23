package com.angular.donationblock.form;

import java.sql.Timestamp;

public class ReportForm 
{
	private long campaignId;
	private long userId;
	private String detail;
	private Timestamp reportTime;
	private String campaignName;
	
	public ReportForm()
	{
		
	}
	public ReportForm(long campaignId,long userId,String detail,Timestamp reportTime,String campaignName)
	{
		this.campaignId=campaignId;
		this.userId=userId;
		this.detail=detail;
		this.reportTime = reportTime;
		this.campaignName = campaignName;
	}
	public Timestamp getReportTime() {
		return reportTime;
	}
	public long getCampaignId() 
	{
		return campaignId;
	}
	public long getUserId() 
	{
		return userId;
	}
	public String getDetail() 
	{
		return detail;
	}
	public String getCampaignName() 
	{
		return campaignName;
	}

}
