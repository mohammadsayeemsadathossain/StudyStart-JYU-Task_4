package com.studystart.jyu.resources;

import com.studystart.jyu.models.RegistrationRequest;
import com.studystart.jyu.models.User;
import com.studystart.jyu.models.UserResponse;
import com.studystart.jyu.services.UserService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Resource for user management
 */
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    
    private UserService userService = new UserService();
    
    /**
     * Register a new user
     */
    @POST
    @Path("/register")
    public Response registerUser(RegistrationRequest request) {
        try {
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Invalid request body"))
                        .build();
            }
            
            User newUser = new User(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName()
            );
            
            UserResponse registeredUser = userService.registerUser(newUser, request.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", registeredUser);
            
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(e.getMessage()))
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Registration failed: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Get all users
     */
    @GET
    public Response getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return Response.ok(users).build();
    }
    
    /**
     * Get specific user by username
     */
    @GET
    @Path("/{username}")
    public Response getUser(@PathParam("username") String username) {
        UserResponse user = userService.getUserResponse(username);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorResponse("User not found"))
                    .build();
        }
        return Response.ok(user).build();
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}