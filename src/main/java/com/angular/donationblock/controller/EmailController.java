package com.angular.donationblock.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.List;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.AccountResponse;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.entity.VerificationToken;
import com.angular.donationblock.form.UserForm;
import com.angular.donationblock.repository.UserRepository;
import com.angular.donationblock.repository.VerificationTokenRepository;

@RestController
@CrossOrigin
public class EmailController
{
	@Autowired
	private VerificationTokenRepository tokenRepo;
	@Autowired
	private UserRepository userRepo;

	private Server server;
	private Scanner scanner;
	
	private void RemoveExpiredToken() throws ParseException
	{
		Date currentDate = getCurrentDate();
		List<VerificationToken> allToken = (List<VerificationToken>) tokenRepo.findAll();
		for(VerificationToken checkToken : allToken)
		{
			if(checkToken.getExpiryDate().compareTo(currentDate) < 0)
			{
				tokenRepo.delete(checkToken);
			}
		}
	}
	private Date getCurrentDate() throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        String tmp = formatter.format(new Date(cal.getTime().getTime()));
        /*Get current date*/
        Date currentDate = formatter.parse(tmp);
        return currentDate;
	}
	/**
	 * This method use to activate user after user clicking on verification link
	 * @throws ParseException
	 * */
	@GetMapping("/activate")
	public RedirectView activateuser(@RequestParam String token) throws ParseException
	{
		RedirectView redirectView = new RedirectView();
		VerificationToken verificationToken = null;
		Date currentDate = getCurrentDate();
		User user;
		System.out.println("Enter Token");
		verificationToken = tokenRepo.findByToken(token);
		if(!(verificationToken == null))
		{
			if(verificationToken.getExpiryDate().compareTo(currentDate) < 0)
			{
				tokenRepo.delete(verificationToken);
			}
			else
			{
				System.out.println("found Token");
				System.out.println("Get user Complete "+verificationToken.getUser().getId()+" "+ verificationToken.getUser().getUsername());
				user = verificationToken.getUser();
				user.setEnabled(true);
				user.setPrivilegeLevel(1);
				System.out.println(verificationToken.getUser().isEnabled());
				System.out.println("Activate Complete");
				userRepo.save(user);
			    tokenRepo.delete(verificationToken);
			}
		}
		redirectView.setUrl("http://34.87.165.176/sign-in");
		return redirectView;
	}
	/**
	 * This method use to sending a email verification link to user from the given email
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * */
	@PostMapping("/sendmail")
	public boolean sendMail(@RequestBody String userId) throws MalformedURLException, IOException
	{
		long id = Long.parseLong(userId);
		System.out.println(userId);
		String token = UUID.randomUUID().toString();
		User user = userRepo.findById(id).get();
		System.out.println("Verification "+user.getRouteImageVerification());
		System.out.println("Route "+user.getRouteSignatureImage());
		String privateKey = null;
		tokenRepo.findAllById(user.getId());

		System.out.println(tokenRepo.findAllByUserId(id).size());
		VerificationToken prevToken = tokenRepo.findByUserId(id);
		if(prevToken != null)
		{
			System.out.println("Deleting");
			tokenRepo.deleteById(prevToken.getId());
		}
		/**
		 * Create stellar account
		 * */
		
    	server = new Server(StellarConfig.stellarServer);
    	KeyPair pair = KeyPair.random();
    	privateKey = new String(pair.getSecretSeed());
		System.out.println(new String(pair.getSecretSeed()));
		System.out.println(pair.getAccountId());
		String friendbotUrl = String.format("https://friendbot.stellar.org/?addr=%s",pair.getAccountId());
		InputStream response = new URL(friendbotUrl).openStream();
		System.out.println(pair.getAccountId());
		scanner = new Scanner(response, "UTF-8");
		String body = scanner.useDelimiter("\\A").next();
		System.out.println("SUCCESS! You have a new account :)\n" + body);
		for(int i = 0; i < 10;i++)
		{
			try
			{
				AccountResponse account = server.accounts().account(pair.getAccountId());
				System.out.println("Balances for account " + pair.getAccountId());
				for (AccountResponse.Balance balance : account.getBalances()) 
				{
				  System.out.println(String.format(
				    "Type: %s, Code: %s, Balance: %s",
				    balance.getAssetType(),
				    balance.getAssetCode(),
				    balance.getBalance()));
				}
				break;
			}
			catch(ErrorResponse e)
			{
				if(i == 9)
				{
					return false;
				}
				System.out.println(e.getBody());
			} 
			catch (IOException e) 
			{
				
				e.printStackTrace();
				return false;
			}
		}
		user.setPublicKey(pair.getAccountId());
		System.out.println(user.getUsername());
		VerificationToken verificationToken = new VerificationToken(token,user);
		tokenRepo.save(verificationToken);
		System.out.println(token);
		System.out.println("Creating email");
		/* Setting property for Gmail*/
		Properties mailProperties = new Properties();
		mailProperties.put("mail.smtp.auth", "true");
		mailProperties.put("mail.smtp.starttls.enable","true");
		mailProperties.put("mail.smtp.host","smtp.gmail.com");
		mailProperties.put("mail.smtp.port","587");
		/* Current system Gmail*/
		String myAccountEmail = "stellardonation053@gmail.com";
		String password = "aeing785329";
		System.out.println("before authenticate");
		Session session = Session.getInstance(mailProperties, new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(myAccountEmail,password);
					}
				});
		System.out.println("After authenticate");
		Message message = prepareMessage(session,myAccountEmail,user.getEmail(),token,user.getUsername(),privateKey,pair.getAccountId());
		try
		{
			Transport.send(message);
			System.out.println("Message send");
			return true;
		}
		catch (MessagingException e)
		{
			System.out.println("send message error");
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * This method use to create a email instance
	 * */
	private Message prepareMessage(Session session,String myAccountEmail,String recepient,String token, String username, 
			String privateKey, String publicKey)
	{
		Message message = new MimeMessage(session);

		try
		{
			message.setFrom(new InternetAddress(myAccountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject("Email Verification");
			message.setText("IMPORTANT : PLEASE KEEP THIS EMAIL STRICTLY CONFIDENTIAL\n\n"
					+ "ID : " + username+"\n"
					+ "Public Key : " + publicKey+"\n\n"
					+ "---------------------------------------------------------------------------------------------------------------------------\n\n"
					+ "REVEAL PRIVATE KEY\n\n"
					+ "Please keep your Private key safe and don't share it with anyone. Private key gives direct access to your money.\n"
					+ "Private key will be use for a future reference while you donate money.\n\n"
					+ "Private Key : " + privateKey+"\n\n"
					+ "---------------------------------------------------------------------------------------------------------------------------\n\n"
					+ "Please verify your email address by clicking on the below link\n"
					+ "http://34.87.165.176:8080/activate?token="+token);
			return message;
		}
		catch (MessagingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
