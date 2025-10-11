package com.studystart.jyu.resources;

import com.studystart.jyu.models.LinkRef;
import com.studystart.jyu.models.RegistrationRequest;
import com.studystart.jyu.models.User;
import com.studystart.jyu.models.UserResponse;
import com.studystart.jyu.services.UserService;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.studystart.jyu.resources.LinkBuilder.link;

/**
 * REST Resource for user management
 */
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	@Context UriInfo uriInfo;
    @Context SecurityContext sc;
    
	private final UserService userService = new UserService();

    /**
     * Register a new user
     * Public: guests are allowed to register.
     */
    @POST
    @Path("/register")
    @PermitAll
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
            List<LinkRef> links = new ArrayList<>();
            links.add(link(uriInfo, "users/" + registeredUser.getUsername(), "self"));
            links.add(link(uriInfo, "profiles/" + registeredUser.getUsername() + "/documents", "documents"));
            registeredUser.setLinks(links);

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
     * Public: visible to guests (e.g., sanitized list).
     */
    @GET
    @PermitAll
    public Response getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        
        boolean isAdmin = sc != null && sc.isUserInRole("ADMIN");

        if (isAdmin) {
            for (UserResponse u : users) {
                String selfHref = uriInfo.getBaseUriBuilder()
                        .path("users").path(u.getUsername())
                        .build()
                        .toString();

                String docsHref = uriInfo.getBaseUriBuilder()
                        .path("profiles").path(u.getUsername()).path("documents")
                        .build()
                        .toString();

                List<LinkRef> links = new ArrayList<>();
                links.add(new LinkRef(selfHref, "self"));
                links.add(new LinkRef(docsHref, "documents"));
                links.add(new LinkRef(selfHref, "update"));

                boolean targetIsAdmin = u.getRoles() != null && u.getRoles().stream()
                        .anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
                
                if (isAdmin && !targetIsAdmin) {
                    links.add(new LinkRef(selfHref, "delete"));
                }

                u.setLinks(links);
            }
        }
        return Response.ok(users).build();
    }

    /**
     * Get specific user by username
     * Only USER (own profile) or ADMIN can access.
     */
    @GET
    @Path("/{username}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getUser(@PathParam("username") String username,
                            @Context SecurityContext securityContext) {
        UserResponse user = userService.getUserResponse(username);
        
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorResponse("User not found"))
                    .build();
        }
        
        List<LinkRef> links = new ArrayList<>();
        links.add(link(uriInfo, "users/" + user.getUsername(), "self"));
        links.add(link(uriInfo, "profiles/" + user.getUsername() + "/documents", "documents"));
        user.setLinks(links);

        // If ADMIN -> allow any profile
        if (securityContext.isUserInRole("ADMIN")) {
            return Response.ok(user).build();
        }

        // If USER -> allow only own profile
        if (isSelf(securityContext, username)) {
            return Response.ok(user).build();
        }

        // Authenticated but not allowed to access another user's profile
        return Response.status(Response.Status.FORBIDDEN)
                .entity(createErrorResponse("Not allowed to access this profile"))
                .build();
    }

    /**
     * (Optional) Delete user
     * Only ADMIN can delete.
     */
    @DELETE
    @Path("/{username}")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("username") String username) {
        boolean ok = userService.deleteUser(username);
        if (ok) return Response.noContent().build();
        return Response.status(Response.Status.NOT_FOUND)
                .entity(createErrorResponse("User not found"))
                .build();
    }

    @GET
    @Path("/deny-test")
    @DenyAll
    public Response denyAllDemo() {
        return Response.ok("this should never be returned").build();
    }
    // --- Helpers ---

    /**
     * Returns true if the authenticated principal matches the requested username.
     * Assumes SecurityContext.getUserPrincipal() is com.studystart.jyu.models.User (set by AuthenticationFilter).
     */
    private boolean isSelf(SecurityContext sc, String requestedUsername) {
        if (sc == null || sc.getUserPrincipal() == null) return false;
        if (!(sc.getUserPrincipal() instanceof com.studystart.jyu.models.User)) return false;
        com.studystart.jyu.models.User me = (com.studystart.jyu.models.User) sc.getUserPrincipal();
        return requestedUsername != null && requestedUsername.equalsIgnoreCase(me.getUsername());
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
