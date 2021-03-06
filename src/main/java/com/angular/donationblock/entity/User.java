package com.angular.donationblock.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User extends BaseEntity 
{
	private String username;
    private String password;
	private String firstName;
    private String lastName;
    private String email;
    private boolean verificationFlag;
    private int privilegeLevel;
    private String publicKey;
    private String routeSignatureImage;
    private String routeImageVerification;
    private boolean enabled = false;
    private boolean active = false;
    private String routeUserImage;

    public User()
    {
    	
    }
    public User(String firstName,
    		String lastName,
    		String email,
    		String username,
    		String password,
    		boolean verificationFlag)
    {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.email = email;
    	this.username = username;
    	this.password = password;
    	this.verificationFlag = verificationFlag;
    	this.privilegeLevel = 0;
    }
    public boolean isActive()
    {
    	return this.active;
    }
    public void setPublicKey(String publicKey)
    {
    	this.publicKey = publicKey;
    }
    public void setActive(Boolean active)
    {
    	this.active = active;
    }
    public boolean isEnabled() 
    {
		return this.enabled;
	}
	public void setEnabled(boolean enabled) 
	{
		System.out.println("set "+this.username+" to "+enabled);
		this.enabled = enabled;
		System.out.println("Result "+this.username+" to "+enabled);
	}

    public User(String firstName,
    		String lastName,
    	    String email,
    	    String username,
    	    String password)
    {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.email = email;
    	this.username = username;
    	this.password = password;
    	this.verificationFlag = false;
    	this.privilegeLevel = 0;
    }
    public String getUsername() 
    {
		return username;
	}
	public void setUsername(String username) 
	{
		this.username = username;
	}
	public String getPassword() 
	{
		return password;
	}
	public void setPassword(String password) 
	{
		this.password = password;
	}
	public String getEmail() 
	{
		return email;
	}
	public void setEmail(String email) 
	{
		this.email = email;
	}
	public boolean isVerificationFlag() 
	{
		return verificationFlag;
	}
	public void setVerificationFlag(boolean verificationFlag) 
	{
		this.verificationFlag = verificationFlag;
	}
	public int getPrivilegeLevel() 
	{
		return privilegeLevel;
	}
	public void setPrivilegeLevel(int privilegeLevel) 
	{
		this.privilegeLevel = privilegeLevel;
	}
	public String getRouteSignatureImage() 
	{
		return routeSignatureImage;
	}
	public void setRouteSignatureImage(String routeSignatureImage) 
	{
		this.routeSignatureImage = routeSignatureImage;
	}
	public String getRouteImageVerification() 
	{
		return routeImageVerification;
	}
	public void setRouteImageVerification(String routeImageVerification) 
	{
		this.routeImageVerification = routeImageVerification;
	}
	public String getFirstName() 
	{
		return firstName;
	}
	public String getLastName() 
	{
		return lastName;
	}
	public String getPublicKey() 
	{
		return publicKey;
	}
	public String getRouteUserImage()
	{
		return routeUserImage;
	}
	public void setRouteUserImage(String routeUserImage)
	{
		this.routeUserImage= routeUserImage;
	}


}