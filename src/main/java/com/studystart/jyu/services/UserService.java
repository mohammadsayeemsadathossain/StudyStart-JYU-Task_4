package com.studystart.jyu.services;

import com.studystart.jyu.models.User;
import com.studystart.jyu.models.UserResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class handling all user-related operations
 */
public class UserService {
    
    private static Map<String, User> userDatabase = new ConcurrentHashMap<>();
    private static Map<String, String> emailToUsername = new ConcurrentHashMap<>();
    
    // Initialize with default admin user
    static {
        User admin = new User("admin", "admin@studystart.jyu.fi", "Admin", "User");
        admin.setPasswordHash(hashPassword("admin123"));
        admin.addRole("ADMIN");
        admin.addRole("USER");
        userDatabase.put(admin.getUsername(), admin);
        emailToUsername.put(admin.getEmail(), admin.getUsername());
    }
    
    /**
     * Register a new user
     */
    public UserResponse registerUser(User user, String plainPassword) {
        validateRegistration(user, plainPassword);
        
        String passwordHash = hashPassword(plainPassword);
        user.setPasswordHash(passwordHash);
        user.addRole("USER");
        
        userDatabase.put(user.getUsername(), user);
        emailToUsername.put(user.getEmail(), user.getUsername());
        
        System.out.println("User registered: " + user.getUsername());
        
        return new UserResponse(user);
    }
    
    /**
     * Validate registration data
     */
    private void validateRegistration(User user, String plainPassword) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        if (userDatabase.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (emailToUsername.containsKey(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    /**
     * Hash password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    /**
     * Verify password
     */
    public boolean verifyPassword(String plainPassword, String passwordHash) {
        return BCrypt.checkpw(plainPassword, passwordHash);
    }
    
    /**
     * Check if credentials are valid
     */
    public boolean userCredentialExists(String username, String password) {
        User user = userDatabase.get(username);
        if (user == null) {
            return false;
        }
        return verifyPassword(password, user.getPasswordHash());
    }
    
    /**
     * Get user with password hash (for authentication)
     */
    public User getUserWithPassword(String username) {
        return userDatabase.get(username);
    }
    
    /**
     * Get all users as UserResponse objects
     */
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = new ArrayList<>();
        for (User user : userDatabase.values()) {
            users.add(new UserResponse(user));
        }
        return users;
    }
    
    /**
     * Get specific user as UserResponse
     */
    public UserResponse getUserResponse(String username) {
        User user = userDatabase.get(username);
        if (user == null) {
            return null;
        }
        return new UserResponse(user);
    }
    
    /**
     * Update user
     */
    public UserResponse updateUser(String username, User updatedUser) {
        User existingUser = userDatabase.get(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        
        return new UserResponse(existingUser);
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(String username) {
        User removed = userDatabase.remove(username);
        if (removed != null) {
            emailToUsername.remove(removed.getEmail());
            return true;
        }
        return false;
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userDatabase.containsKey(username);
    }
}