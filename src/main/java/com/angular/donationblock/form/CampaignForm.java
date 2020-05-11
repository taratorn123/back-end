package com.angular.donationblock.form;

import javax.persistence.Lob;

public class CampaignForm
{
    private Long campaignId;
    private Long userId;
    private String coverImagePath;
    private String targetDonation;
    private String campaignName;
    private String category;
    private String fundRaisingAs;
    private Long donateTimes;
    @Lob
    private String campaignDetail;

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
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

    public String getCampaignDetail() {
        return campaignDetail;
    }

    public void setCampaignDetail(String campaignDetail) {
        this.campaignDetail = campaignDetail;
    }

    public Long getDonateTimes() {
        return donateTimes;
    }

    public void setDonateTimes(Long donateTimes) {
        this.donateTimes = donateTimes;
    }
}
