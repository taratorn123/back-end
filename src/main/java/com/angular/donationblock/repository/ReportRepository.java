package com.angular.donationblock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.angular.donationblock.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long>
{
	List<Report> findAllByCampaignId(long campaignId);

}
