package com.angular.donationblock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.entity.Campaign;

public interface AccountDonationRepository extends JpaRepository<AccountDonation, Long> 
{
}
