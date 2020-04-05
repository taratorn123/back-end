package com.angular.donationblock.controller;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.form.UserForm;
import com.angular.donationblock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.AccountResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import javax.validation.ValidationException;

@RestController
@CrossOrigin
public class UserController 
{

    // standard constructors


    @Autowired
    private UserRepository userRepository;
    
	private Server server;
	private Scanner scanner;

    @GetMapping("/users")
    public List<User> getUsers() 
    {
        return (List<User>) userRepository.findAll();
    }

    @GetMapping("/usersAll")
    public List<User> getUsersAll() 
    {
        userRepository.findAll().forEach(System.out::println);
        return (List<User>) userRepository.findAll();
    }

    @PostMapping("/findByUsername")
    public boolean findByUsername(@RequestBody User user) 
    {
    	System.out.println("Searching for user");
        User userRepo  = userRepository.findByUsername(user.getUsername());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(null != userRepo) 
        {
            if (encoder.matches(user.getPassword(), userRepo.getPassword())) 
            {
            	System.out.println("User found");
                return true;
            }
        }
        System.out.println("User not found");
        return false;
    }

    @GetMapping("/getUserId/{username}")
    public String getUserId(@PathVariable String username)
    {
    	User userRepo = userRepository.findByUsername(username);
    	System.out.println(userRepo.getId());
    	return userRepo.getId().toString();
    }
    
    @GetMapping("/checkVerification/{id}")
    public int getVerification(@PathVariable long id)
    {
    	User userRepo = userRepository.findById(id).get();
    	if(!userRepo.isEnabled())
    	{
    		return 0;
    	}
    	return 1;
    }
    
    
    /**
     * This method use to received userForm from frontend and send it to Stellar network
     * then save result into database
     * */
    
    @PostMapping("/users")
    public long addUser(@RequestBody UserForm userForm) throws MalformedURLException, IOException 
    {
    	User user;
    	server = new Server(StellarConfig.stellarServer);
    	User userRepo = userRepository.findByUsername(userForm.getUsername());
    	if(userRepo != null)
    	{
    		return 0;
    	}
    	System.out.println("This is user username   "+userForm.getUsername());
    	KeyPair pair = KeyPair.random();
		System.out.println(new String(pair.getSecretSeed()));
		System.out.println(pair.getAccountId());
		String friendbotUrl = String.format("https://friendbot.stellar.org/?addr=%s",pair.getAccountId());
		InputStream response = new URL(friendbotUrl).openStream();
		System.out.println(pair.getAccountId());
		scanner = new Scanner(response, "UTF-8");
		String body = scanner.useDelimiter("\\A").next();
		System.out.println("SUCCESS! You have a new account :)\n" + body);
		while(true)
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
		        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		        if(userForm.isVerificationFlag())
		        {
		        	user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
		        			userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()),
		        			pair.getAccountId(),userForm.getRouteSignatureImage(),userForm.getRouteImageVerification());
		        	userRepository.save(user);
		        	System.out.println("Saving database with image path");
		        }
		        else
		        {
		        	user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
		        			userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()),
		        			pair.getAccountId());
		        	userRepository.save(user);
		        	System.out.println("Saving database");
		        }
		        break;
			}
			catch(ErrorResponse e)
			{
				System.out.println(e.getBody());
			}
		}
        return user.getId();
    }
}