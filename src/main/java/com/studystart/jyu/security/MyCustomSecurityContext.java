package com.studystart.jyu.security;

import com.studystart.jyu.models.User;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class MyCustomSecurityContext implements SecurityContext {

    private final User user;
    private final String scheme;

    public MyCustomSecurityContext(User user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (user == null) {
            return Role.GUEST.name().equals(role);
        }
        try {
            return user.getRoles() != null && user.getRoles().contains(Role.valueOf(role));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return (user == null) ? null : SecurityContext.BASIC_AUTH;
    }
}
