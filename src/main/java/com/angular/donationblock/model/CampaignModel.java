package com.angular.donationblock.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.angular.donationblock.entity.User;

public class CampaignModel 
{

	private Long id;
    private boolean deleted;
	private User user;
	private String targetDonation;
    private String campaignName;
    private String category;
    private String fundRaisingAs;
    private Date startDate;
    private String campaignDetail;
    private String coverImagePath;
    private boolean active;
    private String currentTotalDonate;
    
	public CampaignModel()
    {
    	
    }
    public CampaignModel(long id,
    boolean deleted,
    User user,
	String targetDonation,
    String campaignName,
    String category,
    String fundRaisingAs,
    Date startDate,
    String campaignDetail,
    String coverImagePath,
    boolean active,
    String currentTotalDonate)
    {
    	this.id = id;
    	this.deleted = deleted;
    	this.user =  user;
    	this.targetDonation = targetDonation;
    	this.campaignName = campaignName;
    	this.category = category;
    	this.fundRaisingAs = fundRaisingAs;
    	this.startDate = startDate;
    	this.campaignDetail = campaignDetail;
    	this.coverImagePath = coverImagePath;
    	this.active = active;
    	this.currentTotalDonate = currentTotalDonate;
    }
    public Long getId() 
    {
		return id;
	}
	public void setId(Long id) 
	{
		this.id = id;
	}
	public boolean isDeleted() 
	{
		return deleted;
	}
	public void setDeleted(boolean deleted) 
	{
		this.deleted = deleted;
	}
	public void setCurrentTotalDonate(String currentTotalDonate) 
	{
		this.currentTotalDonate = currentTotalDonate;
	}
    public String getCurrentTotalDonate() 
    {
		return currentTotalDonate;
	}
    public void setUser(User user)
    {
        this.user = user;
    }
    public User getUser()
    {
        return user;
	}
    public boolean isActive()
    {
        return this.active;
    }
    public void setActive(boolean active)
    {
        this.active = active;
    }
    public String getTargetDonation() 
    {
        return targetDonation;
    }

    public void setTargetDonation(String targetDonation) 
    {
        this.targetDonation = targetDonation;
    }

    public String getCampaignName() 
    {
        return campaignName;
    }

    public void setCampaignName(String campaignName) 
    {
        this.campaignName = campaignName;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getFundRaisingAs() 
    {
        return fundRaisingAs;
    }

    public void setFundRaisingAs(String fundRaisingAs) 
    {
        this.fundRaisingAs = fundRaisingAs;
    }

    public Date getStartDate() 
    {
        return startDate;
    }

    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public String getCampaignDetail() 
    {
        return campaignDetail;
    }

    public void setCampaignDetail(String campaignDetail) 
    {
        this.campaignDetail = campaignDetail;
    }

    public String getCoverImagePath() 
    {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) 
    {
        this.coverImagePath = coverImagePath;
    }
    // standard constructors / setters / getters / toString
}
