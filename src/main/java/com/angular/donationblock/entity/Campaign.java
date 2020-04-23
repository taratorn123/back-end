package com.angular.donationblock.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Campaign extends BaseEntity
{
	@ManyToOne(cascade = CascadeType.MERGE)
	private User user;

	private String targetDonation;
    private String campaignName;
    private String category;
    private String fundRaisingAs;

    @Lob
    private String campaignDetail;
    private String coverImagePath;
    private boolean active;
    
    

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
    public void setActive(Boolean active)
    {
    	this.active = active;
    }
    public String getTargetDonation() {
        return targetDonation;
    }

    public void setTargetDonation(String targetDonation) {
        this.targetDonation = targetDonation;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFundRaisingAs() {
        return fundRaisingAs;
    }

    public void setFundRaisingAs(String fundRaisingAs) {
        this.fundRaisingAs = fundRaisingAs;
    }

    public String getCampaignDetail() { return campaignDetail; }

    public void setCampaignDetail(String campaignDetail) { this.campaignDetail = campaignDetail; }

    public String getCoverImagePath() { return coverImagePath; }

    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }


    public Campaign(){}
    // standard constructors / setters / getters / toString

}
