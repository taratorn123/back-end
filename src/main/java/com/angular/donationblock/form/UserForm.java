package com.angular.donationblock.form;

public class UserForm 
{
	    private String firstName;
	    private String lastName;
	    private String email;
	    private String username;
	    private String password;
	    private boolean verificationFlag;
	    private String routeSignatureImage;
	    private String routeImageVerification;
	    
	    public UserForm(String firstName,
	    String lastName,
	    String email,
	    String username,
	    String password,
	    boolean verificationFlag,
	    String routeSignatureImage,
	    String routeImageVerification)
	    {
	    	this.firstName = firstName;
	    	this.lastName = lastName;
	    	this.email = email;
	    	this.username = username;
	    	this.password = password;
	    	this.verificationFlag = verificationFlag;
	    	this.routeSignatureImage = routeSignatureImage;
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

		public String getEmail() 
		{
			return email;
		}

		public String getUsername() 
		{
			return username;
		}

		public String getPassword() 
		{
			return password;
		}

		public boolean isVerificationFlag() 
		{
			return verificationFlag;
		}

		public String getRouteSignatureImage() 
		{
			return routeSignatureImage;
		}

		public String getRouteImageVerification() 
		{
			return routeImageVerification;
		}
		public void test()
		{
			
		}
}
