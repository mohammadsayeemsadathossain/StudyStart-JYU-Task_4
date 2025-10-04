package com.studystart.jyu.security;

import com.studystart.jyu.models.User;
import com.studystart.jyu.services.UserService;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication Filter - intercepts ALL incoming requests
 * Implements HTTP Basic Authentication
 * 
 * Filter execution flow:
 * 1. Request comes in
 * 2. Filter checks Authorization header
 * 3. If valid credentials -> set SecurityContext and allow request
 * 4. If invalid/missing -> return 401 Unauthorized
 */
@Provider  // This annotation tells Jersey to use this class as a filter
public class AuthenticationFilter implements ContainerRequestFilter {
    
    // Constants for authentication
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";
    
    // UserService to validate credentials
    private UserService userService = new UserService();
    
    /**
     * This method is called for EVERY request before it reaches your resources
     * 
     * @param requestContext Contains all request information (headers, path, etc.)
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        // Get the Authorization header from the request
        List<String> authHeaders = requestContext.getHeaders().get(AUTHORIZATION_HEADER);
        
        // Check if Authorization header exists
        if (authHeaders == null || authHeaders.isEmpty()) {
            // No authorization header - return 401 Unauthorized
            abortWithUnauthorized(requestContext, "Missing authorization header");
            return;
        }
        
        // Get the authorization value
        String authHeader = authHeaders.get(0);
        
        // Check if it starts with "Basic "
        if (!authHeader.startsWith(BASIC_PREFIX)) {
            abortWithUnauthorized(requestContext, "Invalid authorization format");
            return;
        }
        
        // Extract the Base64 encoded credentials
        // Remove "Basic " prefix to get just the encoded string
        String base64Credentials = authHeader.substring(BASIC_PREFIX.length()).trim();
        
        // Decode Base64 to get "username:password"
        String credentials;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            credentials = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            abortWithUnauthorized(requestContext, "Invalid Base64 encoding");
            return;
        }
        
        // Split into username and password
        String[] parts = credentials.split(":", 2);  // Split only at first ":"
        if (parts.length != 2) {
            abortWithUnauthorized(requestContext, "Invalid credentials format");
            return;
        }
        
        String username = parts[0];
        String password = parts[1];
        
        // Validate credentials using UserService
        if (!userService.userCredentialExists(username, password)) {
            abortWithUnauthorized(requestContext, "Invalid username or password");
            return;
        }
        
        // Credentials are valid - get the full user object
        User authenticatedUser = userService.getUserWithPassword(username);
        
        // Get the request scheme (http or https)
        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        
        // Create and set SecurityContext
        // This tells Jersey WHO the authenticated user is
        CustomSecurityContext securityContext = new CustomSecurityContext(authenticatedUser, scheme);
        requestContext.setSecurityContext(securityContext);
        
        // Log successful authentication
        System.out.println("User authenticated: " + username);
        
        // Request continues to the resource method
    }
    
    /**
     * Helper method to abort request with 401 Unauthorized
     * 
     * @param requestContext The request context
     * @param message Error message to return
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        // Create error response
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        
        // Build 401 response
        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .header("WWW-Authenticate", "Basic realm=\"StudyStart JYU API\"")  // Standard Basic Auth header
                .build();
        
        // Abort the request with this response
        requestContext.abortWith(response);
        
        System.out.println("Authentication failed: " + message);
    }
}