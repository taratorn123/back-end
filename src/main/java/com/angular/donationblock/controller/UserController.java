package com.angular.donationblock.controller;

import com.angular.donationblock.entity.User;
import com.angular.donationblock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    // standard constructors


    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<User> getUsers() {

        return (List<User>) userRepository.findAll();
    }

    @GetMapping("/getUserId")
    public String getUserId(Principal principal){
        return userRepository.findByUsername(principal.getName()).getId().toString();
    }
    @GetMapping("/usersAll")
    public List<User> getUsersAll() {
        userRepository.findAll().forEach(System.out::println);
        return (List<User>) userRepository.findAll();
    }

    @PostMapping("/findByUsername")
    public boolean findByUsername(@RequestBody User user) {
        User userRepo  = userRepository.findByUsername(user.getUsername());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(null != userRepo) {
            if (encoder.matches(user.getPassword(), userRepo.getPassword())) {
                return true;
            }
        }
        return false;
    }
//@PostMapping("/findByUsername")
//void findByUsername(@RequestBody User user) {
//    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//    user.setPassword(passwordEncoder.encode(user.getPassword()));
//    userRepository.save(user);
//}

    @PostMapping("/users")
    void addUser(@RequestBody User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}