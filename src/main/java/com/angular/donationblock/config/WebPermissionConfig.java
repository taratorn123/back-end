package com.angular.donationblock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebPermissionConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/index","/register","/edit/**","/images/**","/styles/**","/js/**","/font/**","/plugins/**","/**").permitAll()

//                .antMatchers("/users","/userInfo","/withdraw/**").hasAnyAuthority("USER")

//                .antMatchers("/competitor/**").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .and()
                .csrf().disable() ;
    }
}
