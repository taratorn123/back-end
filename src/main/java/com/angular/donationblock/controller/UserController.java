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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    	server = new Server(StellarConfig.stellarServer);
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
		        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		        if(userForm.isVerificationFlag())
		        {
		        	user = new User(userForm.getFirstName(),userForm.getLastName(),userForm.getEmail(),
		        			userForm.getUsername(),passwordEncoder.encode(userForm.getPassword()),
		        			pair.getAccountId(),userForm.isVerificationFlag());
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
		        return user.getId();
			}
			catch(ErrorResponse e)
			{
				if(i == 9)
				{
					return 0;
				}
				System.out.println(e.getBody());
			}
		}
        return user.getId();
    }

	@GetMapping("/current-user/{username}")
	public User getUserData(@PathVariable String username) {
		User temp = userRepository.findByUsername(username);
		return temp;
	}

	@PostMapping("/current-user/edit")
	public long addCampaign(@RequestBody User user) throws IOException
	{
		userRepository.save(user);
		return 1;
	}
	@PostMapping("/userImageVerification")
	public boolean userImageVerification(@RequestParam("verification") MultipartFile verification,
			@RequestParam("signature") MultipartFile signature,
			@RequestParam Map<String, String> file)
	{
		User user = userRepository.findById(Long.parseLong(file.get("userId"))).get();
        String directoryName = "D:\\GithubJr\\front-end\\src\\assets\\img\\"+user.getId()+"\\verification\\";
        File directory = new File(directoryName);
        if (! directory.exists())
        {
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        File verificationDest = new File(directoryName+"\\verification.jpg\\");
        File signatureDest = new File(directoryName+"\\signature.jpg\\");
        try 
        {
			verification.transferTo(verificationDest);
			signature.transferTo(signatureDest);
		} 
        catch (IllegalStateException | IOException e) 
        {
			e.printStackTrace();
			userRepository.delete(user);
			return false;
		}
		user.setRouteImageVerification("../../assets/img/"+user.getId()+"/verification/verification.jpg");
		user.setRouteSignatureImage("../../assets/img/"+user.getId()+"/verification/signature.jpg");
		userRepository.save(user);
		return true;
	}
	
	@GetMapping("/getverificationrequest")
	public List<User> getVerificationRequest()
	{
		List<User> users = userRepository.findAllByVerificationFlag(true);
		return users;
		
	}

}