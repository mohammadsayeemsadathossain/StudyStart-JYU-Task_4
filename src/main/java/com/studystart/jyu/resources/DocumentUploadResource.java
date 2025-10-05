package com.studystart.jyu.resources;

import com.studystart.jyu.models.Document;
import com.studystart.jyu.models.DocumentType;
import com.studystart.jyu.services.DocumentService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Upload endpoint for raw PDF/JPEG/PNG bodies.
 * Path example: POST /profiles/{profileId}/documents/new/{documentType}/upload
 *
 * RBAC: USER or ADMIN only. Guests cannot upload.
 */
@Path("/profiles/{profileId}/documents/new/{documentType}")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentUploadResource {

	private final DocumentService documentService = new DocumentService();

	// Tomcat's catalina.base/uploads by default (falls back to "./uploads" for other containers)
	private static final java.nio.file.Path UPLOAD_DIR =
			Paths.get(System.getProperty("catalina.base", "."), "uploads");

	private static final java.util.Set<String> ALLOWED_CT =
			new java.util.HashSet<>(java.util.Arrays.asList(
					"application/pdf", "image/jpeg", "image/png"
			));

	private static String extFor(String ct) {
		if ("application/pdf".equalsIgnoreCase(ct)) return ".pdf";
		if ("image/jpeg".equalsIgnoreCase(ct)) return ".jpg";
		if ("image/png".equalsIgnoreCase(ct)) return ".png";
		return "";
	}

	/**
	 * Uploads a raw file body (PDF/JPEG/PNG) and registers a Document entry.
	 */
	@POST
	@Path("/upload")
	@Consumes({ "application/pdf", "image/jpeg", "image/png" })
	@RolesAllowed({ "USER", "ADMIN" })
	public Response uploadFile(@PathParam("profileId") long profileId,
							   @PathParam("documentType") String docTypeRaw,
							   InputStream bodyStream,
							   @Context UriInfo uriInfo,
							   @Context SecurityContext sc,
							   @HeaderParam("Content-Type") String contentType,
							   @HeaderParam("Content-Length") @DefaultValue("0") long clen) throws IOException {

		// RBAC: must be authenticated (USER/ADMIN)
		String me = currentUsername(sc);
		if (me == null) throw new NotAuthorizedException("Authentication required");

		if (bodyStream == null) throw new BadRequestException("PDF, JPEG or PNG body required");
		if (contentType == null || !ALLOWED_CT.contains(contentType.toLowerCase())) {
			throw new NotSupportedException("Unsupported media type. Allowed: application/pdf, image/jpeg, image/png");
		}

		// Validate/normalize document type via enum
		final DocumentType docType = DocumentType.fromString(docTypeRaw); // will throw if invalid

		// Ensure upload dir exists
		Files.createDirectories(UPLOAD_DIR);

		// Store file
		String storedName = System.currentTimeMillis() + "_" + docType.name() + extFor(contentType);
		java.nio.file.Path target = UPLOAD_DIR.resolve(storedName);
		Files.copy(bodyStream, target, StandardCopyOption.REPLACE_EXISTING);

		// Build Document model
		Document doc = new Document();
		doc.setProfileId(profileId);
		doc.setDocumentType(docTypeRaw); // setter maps String -> enum inside the model
		doc.setFileName(storedName);
		doc.setStoragePath(target.toString());
		doc.setContentType(contentType);
		doc.setSizeBytes(clen > 0 ? clen : Files.size(target));
		doc.setStatus("UPLOADED");
		doc.setUploadDate(new Date());

		// Ownership: USER must use self; ADMIN may override ownerUsername (not provided here, so default to admin)
		if (!sc.isUserInRole("ADMIN")) {
			doc.setOwnerUsername(me);
		} else if (doc.getOwnerUsername() == null || doc.getOwnerUsername().isEmpty()) {
			doc.setOwnerUsername(me);
		}

		// Persist via service
		Document created = documentService.addDocument(profileId, doc);

		// HATEOAS links
		addLinks(created, profileId, uriInfo);

		// Location of the created resource: /profiles/{profileId}/documents/{id}
		URI location = uriInfo.getBaseUriBuilder()
				.path("profiles")
				.path(String.valueOf(profileId))
				.path("documents")
				.path(String.valueOf(created.getId()))
				.build();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("document", created);

		return Response.created(location).entity(result).build();
	}

	// --- links & helpers ---

	private void addLinks(Document document, long profileId, UriInfo uriInfo) {
		String selfUri = uriInfo.getBaseUriBuilder()
				.path("profiles")
				.path(Long.toString(profileId))
				.path("documents")
				.path(Long.toString(document.getId()))
				.build()
				.toString();
		document.addLink(selfUri, "self");

		String profileUri = uriInfo.getBaseUriBuilder()
				.path("profiles")
				.path(Long.toString(profileId))
				.build()
				.toString();
		document.addLink(profileUri, "profile");
	}

	private String currentUsername(SecurityContext sc) {
		if (sc != null && sc.getUserPrincipal() instanceof com.studystart.jyu.models.User) {
			com.studystart.jyu.models.User u =
					(com.studystart.jyu.models.User) sc.getUserPrincipal();
			return u.getUsername();
		}
		return null;
	}
}
