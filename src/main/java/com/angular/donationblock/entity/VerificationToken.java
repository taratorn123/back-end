package com.angular.donationblock.entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class VerificationToken extends BaseEntity
{
	
    private static final int EXPIRATION = 60 * 24;
    

    private String token;
   
    @OneToOne(cascade = CascadeType.MERGE)
    private User user;
    private Date expiryDate;
    
    public VerificationToken()
    {
    	
    }
    public VerificationToken(String token,User user)
    {
    	this.token = token;
    	this.user = user;
    	this.expiryDate = calculateExpiryDate(5);
    }
	public String getToken() 
	{
		return token;
	}

	public void setToken(String token) 
	{
		this.token = token;
	}

	public Date getExpiryDate() 
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) 
	{
		this.expiryDate = expiryDate;
	}

	public User getUser() 
	{
		return user;
	}
    public Date calculateExpiryDate(int expiryTimeInMinutes) 
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}