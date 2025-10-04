package com.studystart.jyu.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for User data
 * Separates API responses from internal User entity to avoid serialization issues
 */
public class UserResponse {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private long createdAt;
    
    // Default constructor
    public UserResponse() {
        this.roles = new ArrayList<>();
    }
    
    // Constructor from User object
    public UserResponse(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = new ArrayList<>(user.getRoles());
        this.createdAt = user.getCreatedAt();
    }
    
    // Getters and Setters
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}