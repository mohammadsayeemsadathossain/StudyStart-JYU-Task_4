package com.studystart.jyu.services;

import com.studystart.jyu.models.Document;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory Document service with profile scoping.
 * Keeps the profileId-based API you had, so resources can be /profiles/{profileId}/documents/...
 */
public class DocumentService {

    private static final Map<Long, Document> documents = new ConcurrentHashMap<>();
    private static final AtomicLong idCounter = new AtomicLong(1);

    static {
        // Sample data (adjust or remove as needed)
        Document doc1 = new Document(1L, 1L, "PASSPORT", "passport_mikael.pdf", "VERIFIED");
        Document doc2 = new Document(2L, 1L, "TRANSCRIPT", "transcript_mikael.pdf", "PENDING");
        documents.put(doc1.getId(), doc1);
        documents.put(doc2.getId(), doc2);
        idCounter.set(3);
    }

    /** List all documents for a given profile */
    public List<Document> getAllDocuments(long profileId) {
        return documents.values().stream()
                .filter(d -> d.getProfileId() == profileId)
                .collect(Collectors.toList());
    }

    /** Get one document by id, ensuring it belongs to the profile */
    public Document getDocument(long profileId, long documentId) {
        Document document = documents.get(documentId);
        if (document == null || document.getProfileId() != profileId) {
            throw new NotFoundException("Document with id " + documentId +
                    " not found for profile " + profileId);
        }
        return document;
    }

    /** Create a document under a profile */
    public Document addDocument(long profileId, Document document) {
        // Your model maps String -> DocumentType inside setters; just ensure it's present.
        if (document.getDocumentType() == null) {
            throw new IllegalArgumentException(
                    "documentType is required (allowed: PASSPORT, RP_CARD, ACCEPTANCE_LETTER)");
        }

        long newId = idCounter.getAndIncrement();
        document.setId(newId);
        document.setProfileId(profileId);
        if (document.getUploadDate() == null) document.setUploadDate(new Date());

        documents.put(newId, document);
        return document;
    }

    /** Update a document (keeps profile scoping) */
    public Document updateDocument(long profileId, Document document) {
        if (document.getDocumentType() == null) {
            throw new IllegalArgumentException(
                    "documentType is required (allowed: PASSPORT, RP_CARD, ACCEPTANCE_LETTER)");
        }

        Document existing = documents.get(document.getId());
        if (existing == null || existing.getProfileId() != profileId) {
            throw new NotFoundException("Document with id " + document.getId() +
                    " not found for profile " + profileId);
        }

        // Keep profile scoping consistent
        document.setProfileId(profileId);

        // Optionally preserve fields if they are null in the incoming payload:
        if (document.getFileName() == null) document.setFileName(existing.getFileName());
        if (document.getStatus() == null) document.setStatus(existing.getStatus());
        if (document.getOwnerUsername() == null) document.setOwnerUsername(existing.getOwnerUsername());
        if (document.getUploadDate() == null) document.setUploadDate(existing.getUploadDate());
        if (document.getExpiryDate() == null) document.setExpiryDate(existing.getExpiryDate());
        if (document.getContentType() == null) document.setContentType(existing.getContentType());
        if (document.getSizeBytes() == 0) document.setSizeBytes(existing.getSizeBytes());
        if (document.getStoragePath() == null) document.setStoragePath(existing.getStoragePath());
        if (document.getLinks() == null || document.getLinks().isEmpty()) document.setLinks(existing.getLinks());

        documents.put(document.getId(), document);
        return document;
    }

    /** Remove a document by id for a profile */
    public void removeDocument(long profileId, long documentId) {
        Document document = documents.get(documentId);
        if (document == null || document.getProfileId() != profileId) {
            throw new NotFoundException("Document with id " + documentId +
                    " not found for profile " + profileId);
        }
        documents.remove(documentId);
    }
}
