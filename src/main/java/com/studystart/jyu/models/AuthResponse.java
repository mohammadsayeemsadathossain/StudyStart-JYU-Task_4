package com.studystart.jyu.models;

import java.util.List;

import com.studystart.jyu.security.Role;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String username;
    private List<Role> roles;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn, String username, List<Role> roles) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public String getUsername() { return username; }
    public List<Role> getRoles() { return roles; }
}
