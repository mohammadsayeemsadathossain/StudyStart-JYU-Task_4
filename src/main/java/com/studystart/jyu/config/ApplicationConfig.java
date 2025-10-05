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
        // scan your resources/providers OR list packages explicitly
        packages("com.studystart.jyu.resources", "com.studystart.jyu.security");
        
        // RBAC-annotations (@RolesAllowed/@PermitAll/@DenyAll)
        register(RolesAllowedDynamicFeature.class);

        register(AuthenticationFilter.class);

        // HK2 binding for UserService (singleton)
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(UserService.class).to(UserService.class).in(Singleton.class);
                // or: bindAsContract(UserService.class).in(Singleton.class);
            }
        });

        System.out.println("StudyStart JYU REST API initialized (path=/api)");
        System.out.println("RolesAllowedDynamicFeature + AuthenticationFilter registered");
    }
}
