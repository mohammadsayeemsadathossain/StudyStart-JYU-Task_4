package com.studystart.jyu.resources;

import javax.ws.rs.core.UriInfo;

import com.studystart.jyu.models.LinkRef;

public class LinkBuilder {
    public static LinkRef link(UriInfo uriInfo, String absolutePath, String rel) {
        // absolutePath can be either absolute or context-relative; this normalizes:
        String base = uriInfo.getBaseUri().toString(); // e.g. http://localhost:8080/<context>/api/
        String href = absolutePath.startsWith("http")
                ? absolutePath
                : (base.endsWith("/") ? base : base + "/") + absolutePath;
        return new LinkRef(href.replace("//api//", "/api/"), rel);
    }
}
