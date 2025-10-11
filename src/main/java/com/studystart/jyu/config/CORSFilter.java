package com.studystart.jyu.config;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Provider
@PreMatching
public class CORSFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String FRONTEND_URL = "http://localhost:3000";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            Response.ResponseBuilder responseBuilder = Response.ok()
                    .header("Access-Control-Allow-Origin", FRONTEND_URL)
                    .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Max-Age", "1209600");
            requestContext.abortWith(responseBuilder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", FRONTEND_URL);
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Headers")) {
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Credentials")) {
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Methods")) {
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}
