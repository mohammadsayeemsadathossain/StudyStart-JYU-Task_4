package com.studystart.jyu.resources;

import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.studystart.jyu.models.AuthRequest;
import com.studystart.jyu.models.AuthResponse;
import com.studystart.jyu.models.User;
import com.studystart.jyu.security.JwtUtil;
import com.studystart.jyu.security.Role;
import com.studystart.jyu.services.UserService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private UserService userService;

    private final JwtUtil jwt;

    public AuthResource() {
        String secret = System.getProperty("jwt.secret", System.getenv().getOrDefault("JWT_SECRET", "super-secret-change-me-please-32bytes-min"));
        String issuer = System.getProperty("jwt.issuer", System.getenv().getOrDefault("JWT_ISSUER", "studystart-jyu"));
        long expMinutes = Long.parseLong(System.getProperty("jwt.exp.minutes", System.getenv().getOrDefault("JWT_EXP_MINUTES", "60")));
        this.jwt = new JwtUtil(secret, issuer, expMinutes);
    }

    /**
     * POST /api/auth/login
     * Accept: JSON {username, password} OR Basic header
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(AuthRequest req, @Context HttpHeaders headers) {
        String[] basic = extractCredentialsIfBasic(headers);
        String username;
        String password;

        if (req != null && req.getUsername() != null && req.getPassword() != null) {
            username = req.getUsername();
            password = req.getPassword();
        } else if (basic != null) {
            username = basic[0];
            password = basic[1];
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error("Provide username/password JSON or Basic Authorization header"))
                    .build();
        }

        if (!userService.userCredentialExists(username, password)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error("Invalid username or password"))
                    .build();
        }

        User user = userService.getUserWithPassword(username);
        List<Role> roles = user.getRoles();
        String token = jwt.generateToken(username, roles);
        long expiresIn = Long.parseLong(System.getProperty("jwt.exp.minutes", System.getenv().getOrDefault("JWT_EXP_MINUTES", "60"))) * 60L;

        return Response.ok(new AuthResponse(token, expiresIn, username, roles)).build();
    }

    private String[] extractCredentialsIfBasic(HttpHeaders headers) {
        String auth = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Basic ")) {
            String b64 = auth.substring("Basic ".length()).trim();
            String pair = new String(Base64.getDecoder().decode(b64), java.nio.charset.StandardCharsets.UTF_8);
            int idx = pair.indexOf(':');
            if (idx > 0) return new String[]{ pair.substring(0, idx), pair.substring(idx + 1) };
        }
        return null;
    }

    private String error(String msg) { return "{\"error\":\"" + msg.replace("\"", "\\\"") + "\"}"; }
}
