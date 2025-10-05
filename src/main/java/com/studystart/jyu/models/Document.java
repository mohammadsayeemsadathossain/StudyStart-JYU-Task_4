package com.studystart.jyu.models;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class Document {
    private long id;
    private long profileId;
    private DocumentType documentType;
    private String fileName;
    private String status;
    private String ownerUsername;
    private Date uploadDate;
    private Date expiryDate;
    private List<LinkRef> links = new ArrayList<>();
    
    private String contentType;
    private long   sizeBytes;
    private String storagePath;

    
    public Document() {}
    
    public Document(long id, long profileId, String documentType, 
                   String fileName, String status) {
        this.id = id;
        this.profileId = profileId;
        this.documentType = DocumentType.fromString(documentType);
        this.fileName = fileName;
        this.status = status;
        this.uploadDate = new Date();
    }

    public void addLink(String href, String rel) {
        LinkRef link = new LinkRef(href, rel);
        links.add(link);
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getProfileId() { return profileId; }
    public void setProfileId(long profileId) { this.profileId = profileId; }
    
    public String getDocumentType() {
    	return documentType != null ? documentType.name() : null;
    }
    public void setDocumentType(String documentType) {
    	this.documentType = DocumentType.fromString(documentType);
    	}
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date uploadDate) { this.uploadDate = uploadDate; }
    
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    
    public List<LinkRef> getLinks() { return links; }
    public void setLinks(List<LinkRef> links) { this.links = links; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }


    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
}