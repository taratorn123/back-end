package com.angular.donationblock.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity

public class User extends BaseEntity {

    private String username;
    private String password;



    public User(String name, String email){
        this.username = username;
        this.password = password;
    }

    public User(){}
    // standard constructors / setters / getters / toString


//    public long getUserID() {
//        return id;
//    }

//    public void setUserID(long userID) {
//        this.id = userID;
//    }

    public void setPassword(String password) { this.password = password; }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }




    @Override
    public String toString() {
        return "User{" +
//                "userID=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}