package com.angular.donationblock.entity;

import javax.persistence.Entity;

@Entity
public class Organization extends BaseEntity {
    private String organizationName;
    private String publicKey;
    private String routeSignatureImage;
    private String organizationImagePath;
}
