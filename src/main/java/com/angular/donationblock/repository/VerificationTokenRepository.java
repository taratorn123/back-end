package com.angular.donationblock.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.angular.donationblock.entity.VerificationToken;

@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> 
{
	VerificationToken findByToken(String token);
	VerificationToken findByUserId(Long id);
	List<VerificationToken> findAllById(Long id);
	List<VerificationToken> findAllByUserId(Long id);
}
