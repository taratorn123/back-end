package com.angular.donationblock.entity;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
    	/* Token will be expired in 5 minutes after its created*/
    	this.expiryDate = calculateExpiryDate(5);
    	System.out.println("Verification Token constructor "+this.expiryDate.toString());
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
    	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        String tmp = formatter.format(new Date(cal.getTime().getTime()));
        try 
        {
			Date date = formatter.parse(tmp);
			return date;
		} 
        catch (ParseException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
}