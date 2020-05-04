package com.angular.donationblock.controller;

import com.angular.donationblock.config.StellarConfig;
import com.angular.donationblock.entity.Campaign;
import com.angular.donationblock.entity.User;
import com.angular.donationblock.form.UserForm;
import com.angular.donationblock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.AccountResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import javax.validation.ValidationException;

@RestController
@CrossOrigin
public class UserController 
{

    // standard constructors


    @Autowired
    private UserRepository userRepository;

	private Server server = new Server(StellarConfig.stellarServer);
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
     * This method use to check user existence
     * */
    @PostMapping("/checkUser")
    public int checkUser(@RequestBody UserForm userForm)
    {
    	User user = userRepository.findByUsername(userForm.getUsername());
    	if(user != null)
    	{
    		return 0;
    	}
    	else
    	{
    		user = userRepository.findByEmail(userForm.getEmail());
    		if(user != null)
    		{
    			return 1;
    		}
    	}
    	return 2;
    }
    @PostMapping("/checkPrivilege")
    public int checkPrivilege(@RequestBody long userId)
    {
    	
    	User user = userRepository.findById(userId).get();
		return user.getPrivilegeLevel();
    }
    
    /**
     * This method use to received userForm from frontend and send it to Stellar network
     * then save result into database
     * */
    
    @PostMapping("/users")
    public long addUser(@RequestBody UserForm userForm) throws MalformedURLException, IOException 
    {
    	User user = null;
    	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    	if(userForm.isVerificationFlag())
    	{
    		user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
    				userForm.getUsername(),passwordEncoder.encode(userForm.getPassword())
    				,userForm.isVerificationFlag());
    		userRepository.save(user);
    		System.out.println("Saving database with image path");
    	}
    	else
    	{
    		user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
    				userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()));
    		userRepository.save(user);
    		System.out.println("Saving database");
    	}
    	return user.getId();
    }

	@GetMapping("/current-user/{username}")
	public User getUserData(@PathVariable String username) {
		User temp = userRepository.findByUsername(username);
		return temp;
	}
	
	@PostMapping("/getUser")
	public User getUserById(@RequestBody String userId)
	{
		return userRepository.findById(Long.parseLong(userId)).get();
	}

	@PostMapping("/current-user/edit")
	public long addCampaign(@RequestBody User user) throws IOException
	{
		userRepository.save(user);
		return 1;
	}
	@PostMapping("userImageSignature")
	public boolean userImageSignature(@RequestBody User user)
	{
		User systemUser = userRepository.findById(user.getId()).get();
		systemUser.setRouteSignatureImage(user.getRouteSignatureImage());
		userRepository.save(systemUser);
		return true;
	}
	@PostMapping("/userImageVerification")
	public boolean userImageVerification(@RequestBody User user)
	{
		User systemUser = userRepository.findById(user.getId()).get();
		systemUser.setRouteImageVerification(user.getRouteImageVerification());
		userRepository.save(systemUser);
		return true;

	}
	
	
	@PostMapping("/setUserCoverImage")
	public boolean setUserCoverImage(@RequestBody User user)
	{
		User systemUser = userRepository.findById(user.getId()).get();
		System.out.println("UserController setUserCoverImage : "+user.getRouteUserImage());
		systemUser.setRouteUserImage(user.getRouteUserImage());
		userRepository.save(systemUser);
		return true;
	}
	@GetMapping("/getverificationrequest")
	public List<User> getVerificationRequest()
	{
		List<User> users = userRepository.findAllByVerificationFlag(true);
		return users;
		
	}
	@PostMapping("/approveuseridentity")
	public boolean approveUserIdentity(@RequestBody String userId)
	{
		System.out.println("approveUserIdentity, UserController : Receive");
		User user = userRepository.findById(Long.parseLong(userId)).get();
		user.setPrivilegeLevel(2);
		user.setVerificationFlag(false);
		userRepository.save(user);
		return true;
	}
	@PostMapping("/declineuseridentity")
	public boolean declineUserIdentity(@RequestBody String userId)
	{
		User user = userRepository.findById(Long.parseLong(userId)).get();
		user.setPrivilegeLevel(1);
		user.setVerificationFlag(false);
		userRepository.save(user);
		return true;
	}

	@GetMapping("/getUserBalance/{userId}")
	public String getUserBalance(@PathVariable Long userId) throws IOException {
		DecimalFormat df = new DecimalFormat("#.00");
		AccountResponse account = null;
		User user = userRepository.findById(userId).get();
		try
		{
			account = server.accounts().account(user.getPublicKey());
		}
		catch(ErrorResponse e)
		{
			System.out.println(e.getBody());
		}

		System.out.println("Hello "+ account.getAccountId());
		System.out.println("Balances for account " + account.getAccountId());
		/* Check if user have enough balance to donate money, if not return 0*/
		for (AccountResponse.Balance balance : account.getBalances())
		{
			if(balance.getAssetType().compareTo("native")==0)
			{
				return df.format(Double.parseDouble(balance.getBalance()));
			}
		}
		return null;
	}

}