package com.studystart.jyu.security;

import com.studystart.jyu.models.User;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Custom SecurityContext implementation
 * Stores authenticated user information and provides role-checking
 */
public class CustomSecurityContext implements SecurityContext {
    
    private User user;           // The authenticated user
    private String scheme;       // http or https
    
    /**
     * Constructor
     * @param user The authenticated user
     * @param scheme The request scheme (http/https)
     */
    public CustomSecurityContext(User user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }
    
    /**
     * Returns the authenticated user as Principal
     * This is used by JAX-RS to identify the current user
     */
    @Override
    public Principal getUserPrincipal() {
        return this.user;
    }
    
    /**
     * Checks if the authenticated user has a specific role
     * Used for authorization (Member 3 will use this)
     * 
     * @param role The role to check (e.g., "ADMIN", "USER")
     * @return true if user has the role, false otherwise
     */
    @Override
    public boolean isUserInRole(String role) {
        if (user.getRoles() != null) {
            return user.getRoles().contains(role);
        }
        return false;
    }
    
    /**
     * Checks if the connection is secure (HTTPS)
     * For production, this should return true only for HTTPS
     */
    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }
    
    /**
     * Returns the authentication scheme used
     * We're using BASIC authentication
     */
    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}