package com.angular.donationblock.repository;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.CampaignUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CampaignUpdateRepository extends JpaRepository<CampaignUpdate, Long> {
    List<CampaignUpdate> findAllByCampaignId(Long campaignId);
    CampaignUpdate findTopByOrderByIdDesc();
}
