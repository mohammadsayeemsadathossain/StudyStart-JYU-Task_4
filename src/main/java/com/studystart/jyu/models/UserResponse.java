package com.studystart.jyu.models;

import com.studystart.jyu.security.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<LinkRef> links = new ArrayList<>();

    // Default constructor
    public UserResponse() {
    }

    // Constructor from User object
    public UserResponse(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        // Convert enum roles to String for API response
        this.roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());
        this.createdAt = user.getCreatedAt();
    }

    // Getters and Setters
    
    public List<LinkRef> getLinks() { return links; }
    public void setLinks(List<LinkRef> links) { this.links = links; }

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
