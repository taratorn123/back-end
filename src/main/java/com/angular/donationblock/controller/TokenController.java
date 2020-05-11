package com.angular.donationblock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.angular.donationblock.entity.User;
import com.angular.donationblock.repository.VerificationTokenRepository;

@RestController
@CrossOrigin
public class TokenController 
{
	@Autowired
	private VerificationTokenRepository tokenRepo;
	
	@GetMapping("checkToken/{token}")
	public long checkToken(@PathVariable String token)
	{
		if(tokenRepo.findByToken(token) != null)
		{
			User user = tokenRepo.findByToken(token).getUser();
			System.out.println("TokenController : user "+ user.getUsername()+" Token Found ");
			return user.getId();
		}
		System.out.println("TokenController : Token not found");
		
		return 0;
	}

}
