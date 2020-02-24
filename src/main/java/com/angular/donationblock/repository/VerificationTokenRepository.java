package com.angular.donationblock.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.angular.donationblock.entity.VerificationToken;

@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> 
{
	VerificationToken findByToken(String token);
}
