package com.studystart.jyu.models;

/**
 * Data Transfer Object for registration requests
 * Separates API contract from internal User model
 */
public class RegistrationRequest {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;  // Plain password (will be hashed before storage)
    
    // Default constructor
    public RegistrationRequest() {}
    
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}