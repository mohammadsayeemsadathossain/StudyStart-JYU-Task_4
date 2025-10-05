package com.studystart.jyu.models;

import java.util.List;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn; // seconds
    private String username;
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn, String username, List<String> roles) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
}
