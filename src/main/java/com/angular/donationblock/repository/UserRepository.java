package com.angular.donationblock.repository;

import com.angular.donationblock.entity.User;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    User findByPublicKey(String publicKey);
    User findByEmail(String email);
    List<User> findAllByVerificationFlag(boolean verificationFlag);
    List<User> findAllByEmail(String email);
}