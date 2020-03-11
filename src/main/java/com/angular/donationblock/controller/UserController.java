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
        User userRepo  = userRepository.findByUsername(user.getUsername());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(null != userRepo) 
        {
            if (encoder.matches(user.getPassword(), userRepo.getPassword())) 
            {
                return true;
            }
        }
        return false;
    }
    @PostMapping("/test")
	public int test(@RequestBody UserForm user)
	{
		System.out.println("Test");
		return 1;
	}
//@PostMapping("/findByUsername")
//void findByUsername(@RequestBody User user) {
//    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//    user.setPassword(passwordEncoder.encode(user.getPassword()));
//    userRepository.save(user);
//}

    @PostMapping("/users")
    public int addUser(@RequestBody UserForm userForm) throws MalformedURLException, IOException 
    {
    	Server server = new Server(StellarConfig.stellarServer);
    	User userRepo = userRepository.findByUsername(userForm.getUsername());
    	if(userRepo != null)
    	{
    		return 0;
    	}
    	KeyPair pair = KeyPair.random();
		System.out.println(new String(pair.getSecretSeed()));
		System.out.println(pair.getAccountId());
		String friendbotUrl = String.format("https://friendbot.stellar.org/?addr=%s",pair.getAccountId());
		InputStream response = new URL(friendbotUrl).openStream();
		String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
		System.out.println("SUCCESS! You have a new account :)\n" + body);
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
        	User user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
        			userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()),
        			pair.getAccountId(),userForm.getRouteSignatureImage(),userForm.getRouteImageVerification());
        	userRepository.save(user);
        	System.out.println("Saving database with image path");
        }
        else
        {
        	User user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
        			userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()),
        			pair.getAccountId());
        	userRepository.save(user);
        	System.out.println("Saving database");
        }
        return 1;
    }
}