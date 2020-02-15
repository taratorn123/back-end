package com.angular.donationblock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.angular.donationblock.entity.AccountDonation;

public interface AccountDonationRepository extends JpaRepository<AccountDonation, Long> {
}
