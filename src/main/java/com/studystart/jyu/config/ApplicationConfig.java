package com.studystart.jyu.config;

import com.studystart.jyu.security.AuthenticationFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig() {
        // Register resource packages
        packages("com.studystart.jyu.resources");
        
        // Register JSON Binding feature
        register(org.glassfish.jersey.jsonb.JsonBindingFeature.class);

        // RBAC-annotations (@RolesAllowed/@PermitAll/@DenyAll)
        register(RolesAllowedDynamicFeature.class);

        register(AuthenticationFilter.class);

        System.out.println("StudyStart JYU REST API initialized (path=/api)");
        System.out.println("RolesAllowedDynamicFeature + AuthenticationFilter registered");
        
        //System.out.println("StudyStart JYU REST API initialized");
    }
}
