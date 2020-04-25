package com.angular.donationblock.form;

import java.util.Date;

public class CampaignUpdateForm {
    private Long campaignId;
    private Date updateTimestamp;
    private String campaignUpdateDetail;

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getCampaignUpdateDetail() {
        return campaignUpdateDetail;
    }

    public void setCampaignUpdateDetail(String campaignUpdateDetail) {
        this.campaignUpdateDetail = campaignUpdateDetail;
    }
}
