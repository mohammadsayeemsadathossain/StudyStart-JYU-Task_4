package com.studystart.jyu.security;

import com.studystart.jyu.models.User;
import com.studystart.jyu.services.UserService;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication + RBAC Filter:
 *  - Intercepts all incoming requests
 *  - Supports HTTP Basic Authentication
 *  - Always sets SecurityContext (authenticated user or guest)
 *  - Evaluates @PermitAll, @DenyAll, and @RolesAllowed annotations
 */
@Provider
@Priority(Priorities.AUTHENTICATION) // ensure filter runs in auth phase
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo; // to read method/class annotations

    // Constants for authentication
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";
    private static final String BEARER_PREFIX = "Bearer "; // reserved for future JWT

    // UserService to validate credentials
    private final UserService userService = new UserService();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // === 1) Try to authenticate user ===
        User authenticatedUser = null;

        List<String> authHeaders = requestContext.getHeaders().get(AUTHORIZATION_HEADER);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);

            if (authHeader.startsWith(BASIC_PREFIX)) {
                String base64Credentials = authHeader.substring(BASIC_PREFIX.length()).trim();

                try {
                    String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                    String[] parts = credentials.split(":", 2);
                    if (parts.length == 2) {
                        String username = parts[0];
                        String password = parts[1];
                        if (userService.userCredentialExists(username, password)) {
                            authenticatedUser = userService.getUserWithPassword(username);
                            System.out.println("User authenticated (Basic): " + username);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid Base64: ignore, user remains guest
                }
            } else if (authHeader.startsWith(BEARER_PREFIX)) {
                // TODO: add JWT parsing here in the future
            }
        }

        // === 2) Always set SecurityContext (guest if not authenticated) ===
        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        requestContext.setSecurityContext(new MyCustomSecurityContext(authenticatedUser, scheme));

        // === 3) RBAC: evaluate security annotations ===
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Method-level annotations take priority
        if (method.isAnnotationPresent(DenyAll.class)) {
            abort403(requestContext, "Access blocked for all users.");
            return;
        }
        if (method.isAnnotationPresent(PermitAll.class)) {
            return; // allow for everyone
        }
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            if (!rolesMatch(requestContext, method.getAnnotation(RolesAllowed.class))) {
                handleAuthzFailure(requestContext);
            }
            return;
        }

        // If no method-level annotations, check class-level
        if (resourceClass.isAnnotationPresent(DenyAll.class)) {
            abort403(requestContext, "Access blocked for all users.");
            return;
        }
        if (resourceClass.isAnnotationPresent(PermitAll.class)) {
            return;
        }
        if (resourceClass.isAnnotationPresent(RolesAllowed.class)) {
            if (!rolesMatch(requestContext, resourceClass.getAnnotation(RolesAllowed.class))) {
                handleAuthzFailure(requestContext);
            }
        }
        // If no annotations at all → allow
    }

    /**
     * Checks if the current SecurityContext has at least one of the allowed roles.
     */
    private boolean rolesMatch(ContainerRequestContext ctx, RolesAllowed ra) {
        for (String role : ra.value()) {
            if (ctx.getSecurityContext().isUserInRole(role)) return true;
        }
        return false;
    }

    /**
     * Distinguish between unauthenticated (401) and unauthorized (403).
     */
    private void handleAuthzFailure(ContainerRequestContext ctx) {
        if (ctx.getSecurityContext() == null || ctx.getSecurityContext().getUserPrincipal() == null) {
            abort401(ctx, "Authentication required");
        } else {
            abort403(ctx, "Insufficient role");
        }
    }

    /**
     * Abort with 401 Unauthorized.
     */
    private void abort401(ContainerRequestContext ctx, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .header("WWW-Authenticate", "Basic realm=\"StudyStart JYU API\"")
                .build());
        System.out.println("401 Unauthorized: " + message);
    }

    /**
     * Abort with 403 Forbidden.
     */
    private void abort403(ContainerRequestContext ctx, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .build());
        System.out.println("403 Forbidden: " + message);
    }
}
