package com.angular.donationblock.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Campaign extends BaseEntity {
    private Long campaignID;
    private String targetDonation;
    private String campaignName;
    private String category;
    private String fundRaisingAs;
    @Lob
    private String campaignDetail;
    private String coverImagePath;

//    @OneToOne
//    private Image coverImage;


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

    public Long getCampaignID() { return campaignID; }

    public void setCampaignID(Long campaignID) { this.campaignID = campaignID; }

    public Campaign(){}
    // standard constructors / setters / getters / toString

}
