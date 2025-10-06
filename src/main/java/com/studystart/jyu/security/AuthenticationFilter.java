package com.studystart.jyu.security;

import com.studystart.jyu.models.User;
import com.studystart.jyu.services.UserService;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication + RBAC Filter:
 *  - Supports HTTP Basic and Bearer (JWT)
 *  - Sets SecurityContext (authenticated user or guest)
 *  - Evaluates @PermitAll, @DenyAll, @RolesAllowed
 *  - Uses String JSON error bodies
 */
@Provider
@Priority(Priorities.AUTHENTICATION) // ensure filter runs in auth phase
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo; // to read method/class annotations
    
    // Constants for authentication
    private static final String AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    private static final String BASIC_PREFIX  = "Basic ";
    private static final String BEARER_PREFIX = "Bearer ";
    
    // UserService to validate credentials
    @Inject
    private UserService userService;

    private final JwtUtil jwt;

    public AuthenticationFilter() {
        String secret = System.getProperty("jwt.secret",
                System.getenv().getOrDefault("JWT_SECRET", "super-secret-change-me-please-32bytes-min"));
        String issuer = System.getProperty("jwt.issuer",
                System.getenv().getOrDefault("JWT_ISSUER", "studystart-jyu"));
        long expMinutes = Long.parseLong(System.getProperty("jwt.exp.minutes",
                System.getenv().getOrDefault("JWT_EXP_MINUTES", "60")));
        this.jwt = new JwtUtil(secret, issuer, expMinutes);
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        final String path   = ctx.getUriInfo().getPath();   // e.g. "auth/login"
        final String method = ctx.getMethod();

        // 0) Public endpoints + CORS preflight
        if (isPublic(path, method)) return;

        // 1) Try to authenticate (Bearer first, then Basic)
        User authenticatedUser = null;
        String auth = ctx.getHeaderString(AUTHORIZATION);

        if (auth == null || auth.isEmpty()) {
            // no auth header -> still set guest context; RBAC below will decide 401/403
            setGuestContext(ctx);
        } else {
            try {
                if (auth.startsWith(BEARER_PREFIX)) {
                    authenticatedUser = authenticateBearer(auth.substring(BEARER_PREFIX.length()).trim());
                    System.out.println(authenticatedUser);
                } else if (auth.startsWith(BASIC_PREFIX)) {
                    authenticatedUser = authenticateBasic(auth.substring(BASIC_PREFIX.length()).trim());
                } else {
                    abort401(ctx, "Unsupported Authorization scheme");
                    return;
                }
            } catch (IllegalArgumentException ex) {
            	abort401(ctx, ex.getMessage());
            	return;
            } catch (JwtUtil.JwtExpiredException ex) {
                abort401(ctx, "Token expired");
                return;
            } catch (JwtUtil.JwtException ex) {
                abort401(ctx, "Invalid token");
                return;
            } catch (Exception ex) {
                ctx.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(jsonError("Auth error")).build());
                return;
            }
            setContext(ctx, authenticatedUser);
        }

        // 2) RBAC: evaluate annotations (method first, then class)
        Method m = resourceInfo.getResourceMethod();
        Class<?> rc = resourceInfo.getResourceClass();

        if (m.isAnnotationPresent(DenyAll.class)) {
            abort403(ctx, "Access blocked for all users");
            return;
        }
        if (m.isAnnotationPresent(PermitAll.class)) {
            return;
        }
        if (m.isAnnotationPresent(RolesAllowed.class)) {
            if (!rolesMatch(ctx, m.getAnnotation(RolesAllowed.class))) {
                handleAuthzFailure(ctx);
            }
            return;
        }

        if (rc.isAnnotationPresent(DenyAll.class)) {
            abort403(ctx, "Access blocked for all users");
            return;
        }
        if (rc.isAnnotationPresent(PermitAll.class)) {
            return;
        }
        if (rc.isAnnotationPresent(RolesAllowed.class)) {
            if (!rolesMatch(ctx, rc.getAnnotation(RolesAllowed.class))) {
                handleAuthzFailure(ctx);
            }
        }
        // no annotations -> allow
    }

    // ===== Auth helpers =====

    private boolean isPublic(String path, String method) {
        String p = path == null ? "" : path.toLowerCase();
        if ("options".equalsIgnoreCase(method)) return true; // CORS preflight
        if ("post".equalsIgnoreCase(method) && (p.equals("auth/login") || p.equals("users/register"))) return true;
        return false;
    }

    private User authenticateBasic(String b64) {
        byte[] decoded = Base64.getDecoder().decode(b64);
        String pair = new String(decoded, StandardCharsets.UTF_8);
        int idx = pair.indexOf(':');
        if (idx <= 0) throw new IllegalArgumentException("Malformed Basic credentials");
        String username = pair.substring(0, idx);
        String password = pair.substring(idx + 1);

        if (!userService.userCredentialExists(username, password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        return userService.getUserWithPassword(username);
    }

    private User authenticateBearer(String token) {
        JwtUtil.Decoded decoded = jwt.validateAndDecode(token);
        long now = Instant.now().getEpochSecond();
        boolean isExpired = now >= decoded.exp;
        if (isExpired) {
        	throw new JwtUtil.JwtExpiredException("Token expired");
        }
        
        User u = userService.getUserWithPassword(decoded.username);
        System.out.println(u);
        if (u == null) {
        	throw new JwtUtil.JwtException("Invalid token");
        }
        
        if (!decoded.roles.equals(u.getRoles())) {
        	throw new JwtUtil.JwtException("Invalid token");
        }

        return u;
        
    }

    private void setGuestContext(ContainerRequestContext ctx) {
        setContext(ctx, null); // guest
    }

    private void setContext(ContainerRequestContext ctx, User userOrNull) {
        String scheme = ctx.getUriInfo().getRequestUri().getScheme();
        // Use your existing class name
        ctx.setSecurityContext(new MyCustomSecurityContext(userOrNull, scheme));
    }

    // ===== RBAC helpers =====

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

    // ===== Error utilities (String bodies as you requested) =====

    private void abort401(ContainerRequestContext ctx, String message) {
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"StudyStart JYU API\", charset=\"UTF-8\", Bearer realm=\"StudyStart JYU API\"")
                .entity(jsonError(message))
                .build());
    }

    private void abort403(ContainerRequestContext ctx, String message) {
        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .entity(jsonError(message))
                .build());
    }

    private String jsonError(String msg) {
        return "{\"error\":\"" + (msg == null ? "" : msg.replace("\"", "\\\"")) + "\"}";
    }
}