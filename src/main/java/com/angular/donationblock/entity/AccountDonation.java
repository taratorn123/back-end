package com.angular.donationblock.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
public class AccountDonation extends BaseEntity {
    private String amount;
    private String timestamp;
    private String comment;
    private String transactionHash;
    private String anonymousFlag;

//    @ManyToOne
//    private Campaign campaign;
//
//    @ManyToOne
//    private User user;

}
