package com.angular.donationblock.repository;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long>{

}
