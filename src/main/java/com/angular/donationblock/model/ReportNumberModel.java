package com.angular.donationblock.model;

public class ReportNumberModel 
{

	private long campaignId;
	private String campaignName;
	private int reportNumber;
	
	public ReportNumberModel(long campaignId,
	String campaignName,
	int reportNumber)
	{
		this.campaignId=campaignId;
		this.campaignName=campaignName;
		this.reportNumber=reportNumber;
	}
	public long getCampaignId() 
	{
		return campaignId;
	}

	public String getCampaignName() 
	{
		return campaignName;
	}

	public int getReportNumber() 
	{
		return reportNumber;
	}
}
