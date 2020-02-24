package com.angular.donationblock.controller;

import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

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
	
	@GetMapping("/activate")
	public RedirectView activateuser(@RequestParam String token)
	{
		System.out.println("Enter Token");
		VerificationToken verificationToken = tokenRepo.findByToken(token);
		System.out.println("found Token");
		System.out.println("Get user Complete "+verificationToken.getUser().getId()+" "+ verificationToken.getUser().getUsername());
		verificationToken.getUser().setEnabled(true);
		System.out.println("Activate Complete");
		userRepo.save(verificationToken.getUser());
		RedirectView redirectView = new RedirectView();
	    redirectView.setUrl("http://localhost:4200/sign-in");
	    return redirectView;
	}
	@PostMapping("/sendmail")
	public void sendMail(@RequestBody UserForm user)
	{
		String token = UUID.randomUUID().toString();
		System.out.println(user.getUsername());
		System.out.println(userRepo.findByUsername(user.getUsername()));
		VerificationToken verificationToken = new VerificationToken(token,userRepo.findByUsername(user.getUsername()));
		tokenRepo.save(verificationToken);
		System.out.println(token);
		System.out.println("Creating email");
		Properties mailProperties = new Properties();
		mailProperties.put("mail.smtp.auth", "true");
		mailProperties.put("mail.smtp.starttls.enable","true");
		mailProperties.put("mail.smtp.host","smtp.gmail.com");
		mailProperties.put("mail.smtp.port","587");
		
		String myAccountEmail = "stellardonation053@gmail.com";
		String password = "35e5af785d";
		
		Session session = Session.getInstance(mailProperties, new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(myAccountEmail,password);
					}
				});
		Message message = prepareMessage(session,myAccountEmail,user.getEmail(),token);
		try 
		{
			Transport.send(message);
			System.out.println("Message send");
		} 
		catch (MessagingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Message prepareMessage(Session session,String myAccountEmail,String recepient,String token)
	{
		Message message = new MimeMessage(session);
		
		try 
		{
			message.setFrom(new InternetAddress(myAccountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject("test");
			message.setText("http://localhost:8080/activate?token="+token);
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
