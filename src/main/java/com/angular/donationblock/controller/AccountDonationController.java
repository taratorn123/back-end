package com.angular.donationblock.controller;

import com.angular.donationblock.entity.AccountDonation;
import com.angular.donationblock.repository.AccountDonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AccountDonationController {
    @Autowired
    private AccountDonationRepository accountDonationRepository;

    @PostMapping
    public int addToStellar(@RequestBody AccountDonation accountDonation, String source) {
        return 1;
    }
}
