package com.studystart.jyu.resources;

import com.studystart.jyu.models.Document;
import com.studystart.jyu.services.DocumentService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

/**
 * Document endpoints scoped under a profile:
 *   Base path: /profiles/{username}/documents
 *
 * RBAC:
 *   - GET list / GET one: @PermitAll (guest allowed)
 *   - POST / PUT: @RolesAllowed({"USER","ADMIN"})  (USER may only manage own docs)
 *   - DELETE: @RolesAllowed("ADMIN")
 */
@Path("/profiles/{username}/documents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentResource {

    private final DocumentService documentService = new DocumentService();

    /** Public list (guest/user/admin) */
    @GET
    @PermitAll
    public List<Document> getDocuments(@PathParam("username") String username,
                                       @Context UriInfo uriInfo) {
        List<Document> documents = documentService.getAllDocuments(username);
        for (Document d : documents) {
            addLinks(d, username, uriInfo);
        }
        return documents;
    }

    /** Public get (guest/user/admin). If you want it private, change to @RolesAllowed({"USER","ADMIN"}). */
    @GET
    @Path("/{documentId}")
    @RolesAllowed({"USER","ADMIN"})
    public Document getDocument(@PathParam("username") String username,
                                @PathParam("documentId") long documentId,
                                @Context UriInfo uriInfo) {
        Document document = documentService.getDocument(username, documentId);
        addLinks(document, username, uriInfo);
        return document;
    }

    /** Create: USER or ADMIN. USER can only create for self (owner = current user). */
    @POST
    @RolesAllowed({"USER","ADMIN"})
    public Response addDocument(@PathParam("username") String username,
                                Document document,
                                @Context UriInfo uriInfo,
                                @Context SecurityContext sc) {
        // Must be authenticated
        String me = currentUsername(sc);
        if (me == null) throw new NotAuthorizedException("Authentication required");

        // Enforce ownership: USER -> self; ADMIN may set owner (default to admin if missing)
        if (!sc.isUserInRole("ADMIN")) {
            document.setOwnerUsername(me);
        } else if (document.getOwnerUsername() == null || document.getOwnerUsername().isEmpty()) {
            document.setOwnerUsername(me);
        }

        if (document.getDocumentType() == null) {
            throw new BadRequestException("documentType is required (PASSPORT, RP_CARD, ACCEPTANCE_LETTER)");
        }

        Document newDocument = documentService.addDocument(username, document);
        addLinks(newDocument, username, uriInfo);

        URI uri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(newDocument.getId()))
                .build();
        return Response.created(uri).entity(newDocument).build();
    }

    /** Update: USER (own doc) or ADMIN (any doc). */
    @PUT
    @Path("/{documentId}")
    @RolesAllowed({"USER","ADMIN"})
    public Document updateDocument(@PathParam("username") String username,
                                   @PathParam("documentId") long documentId,
                                   Document document,
                                   @Context UriInfo uriInfo,
                                   @Context SecurityContext sc) {
        // Fetch existing to check ownership
        Document existing = documentService.getDocument(username, documentId);
        if (!(sc.isUserInRole("ADMIN") || isOwner(sc, existing.getOwnerUsername()))) {
            throw new ForbiddenException("Not allowed to modify this document");
        }

        // Keep ID consistent and update
        document.setId(documentId);
        Document updated = documentService.updateDocument(username, document);
        addLinks(updated, username, uriInfo);
        return updated;
    }

    /** Delete: ADMIN only. */
    @DELETE
    @Path("/{documentId}")
    @RolesAllowed("ADMIN")
    public Response deleteDocument(@PathParam("username") String username,
                                   @PathParam("documentId") long documentId) {
        documentService.removeDocument(username, documentId);
        return Response.noContent().build();
    }

    // --- HATEOAS links (simple, string-based) ---

    private void addLinks(Document document, String username, UriInfo uriInfo) {
        // Self: /profiles/{username}/documents/{id}
        String selfUri = uriInfo.getBaseUriBuilder()
                .path("profiles")
                .path(username)
                .path("documents")
                .path(Long.toString(document.getId()))
                .build()
                .toString();
        document.addLink(selfUri, "self");

        // Profile: /profiles/{username}
        String profileUri = uriInfo.getBaseUriBuilder()
                .path("profiles")
                .path(username)
                .build()
                .toString();
        document.addLink(profileUri, "profile");
    }

    // --- helpers ---

    /** Current username from SecurityContext (set by AuthenticationFilter). */
    private String currentUsername(SecurityContext sc) {
        if (sc != null && sc.getUserPrincipal() instanceof com.studystart.jyu.models.User) {
            com.studystart.jyu.models.User u =
                    (com.studystart.jyu.models.User) sc.getUserPrincipal();
            return u.getUsername();
        }
        return null;
    }

    /** Owner check (case-insensitive). */
    private boolean isOwner(SecurityContext sc, String ownerUsername) {
        String me = currentUsername(sc);
        return me != null && ownerUsername != null && me.equalsIgnoreCase(ownerUsername);
    }
}
