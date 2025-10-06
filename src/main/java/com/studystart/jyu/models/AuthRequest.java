package com.studystart.jyu.models;

public class AuthRequest {
    private String username;
    private String password;

    public AuthRequest() {}
    public AuthRequest(String u, String p) { this.username = u; this.password = p; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
