package com.studystart.jyu.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import com.studystart.jyu.security.Role;

/**
 * User entity representing a registered user in the system.
 * Implements Principal for security context integration.
 */
public class User implements Principal {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private List<Role> roles;
    private long createdAt;
    
    // Default constructor
    public User() {
        this.roles = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }
    
    // Constructor for creating new user
    public User(String username, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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
    
    @JsonIgnore  // Prevent serialization - only for internal use
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    // Principal interface implementation
    @JsonIgnore  // CRITICAL: Prevents infinite recursion during JSON serialization
    @Override
    public String getName() {
        return this.firstName + " " + this.lastName;
    }
    
    /**
     * Returns a safe copy of user without password hash
     */
    public User getSafeUser() {
        User safeUser = new User(this.username, this.email, this.firstName, this.lastName);
        safeUser.setRoles(new ArrayList<>(this.roles));
        safeUser.setCreatedAt(this.createdAt);
        return safeUser;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + roles +
                '}';
    }
}