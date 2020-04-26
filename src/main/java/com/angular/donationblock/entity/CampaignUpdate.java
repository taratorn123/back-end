package com.angular.donationblock.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class CampaignUpdate extends BaseEntity {
    @ManyToOne(cascade = CascadeType.MERGE)
    private Campaign campaign;

    private Date updateTimestamp;
    private String campaignUpdateDetail;

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
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
