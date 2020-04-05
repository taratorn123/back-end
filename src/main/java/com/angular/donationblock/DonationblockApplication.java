package com.angular.donationblock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class DonationblockApplication 
{
	public static void main(String[] args) 
	{
		SpringApplication.run(DonationblockApplication.class, args);
	}
}
