package com.studystart.jyu.config;

import com.studystart.jyu.security.AuthenticationFilter;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig() {
        // Register resource packages
        packages("com.studystart.jyu.resources");
        
        // Register JSON Binding feature
        register(org.glassfish.jersey.jsonb.JsonBindingFeature.class);
        
        // CRITICAL: Register Authentication Filter
        register(AuthenticationFilter.class);
        
        System.out.println("StudyStart JYU REST API initialized");
        System.out.println("Authentication Filter registered");
    }
}