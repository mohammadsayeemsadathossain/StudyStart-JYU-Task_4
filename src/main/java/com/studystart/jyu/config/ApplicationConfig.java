package com.studystart.jyu.config;

import com.studystart.jyu.security.AuthenticationFilter;
import com.studystart.jyu.services.UserService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        // Scan REST resource and security packages
        packages("com.studystart.jyu.resources", "com.studystart.jyu.security", "com.studystart.jyu.config");

        // Register role-based access control support
        register(RolesAllowedDynamicFeature.class);

        // Register authentication + CORS filters
        register(AuthenticationFilter.class);
        register(CORSFilter.class);

        // Bind UserService as singleton
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(UserService.class).to(UserService.class).in(Singleton.class);
            }
        });

        System.out.println("✅ StudyStart JYU REST API initialized (path=/api)");
        System.out.println("➡️ RolesAllowedDynamicFeature, AuthenticationFilter, and CORSFilter registered");
    }
}
