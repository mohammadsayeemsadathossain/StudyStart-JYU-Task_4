import React, { useEffect, useState } from "react";
import UploadModal from "../components/UploadModal"; 
import '../styles/Documents.css';
import EditDocumentModal from "../components/EditModal";

interface User {
  username: string;
  roles: string[];
  // Added optional fields to match the saved user object structure
  createdAt?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
}

interface Document {
  id: number;
  username: string;
  documentType: string;
  fileName: string;
  contentType: string;
  status: string;
  uploadDate: string;
}

const API_BASE = process.env.REACT_APP_API_BASE;

const DocumentsPage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editDoc, setEditDoc] = useState<Document | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  const isAdmin = user?.roles.includes("ADMIN");

  useEffect(() => {
    const savedUser = localStorage.getItem("user");
    if (savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser);
        console.log("Parsed user from localStorage:", parsedUser); // Debug log to verify user loading
        setUser(parsedUser);
      } catch (err) {
        console.error("Error parsing user from localStorage:", err);
      }
    }
  }, []);

  useEffect(() => {
    if (user) fetchDocuments();
  }, [user]);

  const fetchDocuments = async () => {
    if (!user) return;
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE}/profiles/${user.username}/documents`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken") || ""}`,
        },
      });
      if (!res.ok) throw new Error("Failed to fetch documents");
      const data = await res.json();
      console.log("Fetched documents data:", data);

      if (Array.isArray(data)) {
        setDocuments(data);
      } else if (Array.isArray(data.items)) {
        // Changed from data.documents to data.items
        setDocuments(data.items);
      } else if (Array.isArray(data.documents)) {
        setDocuments(data.documents);
      } else {
        console.warn("Unexpected data format:", data);
        setDocuments([]);
      }
    } catch (err) {
      console.error("Error fetching documents", err);
      setDocuments([]);
    } finally {
      setLoading(false);
    }
  };


  const handleUpload = async (file: File, type: string) => {
    if (!user) return;

    try {
      setLoading(true);
      const uploadUrl = `${API_BASE}/profiles/${user.username}/documents/new/${type}/`;

      const res = await fetch(uploadUrl, {
        method: "POST",
        headers: {
          "Content-Type": file.type,
          Authorization: `Bearer ${localStorage.getItem("jwtToken") || ""}`,
        },
        body: file,
      });

      if (!res.ok) throw new Error("Upload failed");

      alert("File uploaded successfully!");
      await fetchDocuments();
    } catch (err) {
      console.error("Upload error:", err);
      alert("Failed to upload document.");
    } finally {
      setLoading(false);
      setIsModalOpen(false);
    }
  };

  const handleDelete = async (docId: number) => {
    if (!isAdmin || !user) return;
    if (!window.confirm("Are you sure you want to delete this document?")) return;

    try {
      const res = await fetch(`${API_BASE}/profiles/${user.username}/documents/${docId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken") || ""}`,
        },
      });
      if (!res.ok) throw new Error("Delete failed");
      await fetchDocuments();
    } catch (err) {
      console.error("Delete error:", err);
    }
  };

  const handleEdit = (doc: Document) => {
  setEditDoc(doc);
  setIsEditModalOpen(true);
};

const handleSaveEdit = async (docId: number, updatedDoc: { documentType: string; fileName: string }) => {
  if (!user) return;
  try {
    const res = await fetch(`${API_BASE}/profiles/${user.username}/documents/${docId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("jwtToken") || ""}`,
      },
      body: JSON.stringify(updatedDoc),
    });
    if (!res.ok) throw new Error("Update failed");
    await fetchDocuments();
    setIsEditModalOpen(false);
    setEditDoc(null);
  } catch (err) {
    console.error("Update error:", err);
    alert("Failed to update document.");
  }
};

  
  console.log("Render check - User:", user, "isAdmin:", isAdmin, "Has USER role:", user?.roles.includes("USER"));

  return (
    <div className="documents-container">
      <h1 className="documents-heading">Documents</h1>

      {loading && <p className="loading-message">Loading...</p>}

      {user && (
        <p className="logged-in-message">
          Logged in as: <strong>{user.username}</strong> ({user.roles.join(", ")})
        </p>
      )}

      {/* Upload button */}
      {user && (user.roles.includes("USER") || isAdmin) && (
        <div className="upload-section">
          <button
            onClick={() => setIsModalOpen(true)}
            className="upload-button"
          >
            Upload new document
          </button>
        </div>
      )}

      {/* Upload modal */}
      <UploadModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onUpload={handleUpload}
      />

      <div className="documents-list">
        {documents.map((doc) => (
          <div key={doc.id} className="document-card">
            <div className="document-info">
              <p className="document-file"><strong>File:</strong> {doc.fileName}</p>
              <p className="document-type"><strong>Type:</strong> {doc.documentType}</p>
              <p className="document-status"><strong>Status:</strong> {doc.status}</p>
              <p className="document-date"><strong>Uploaded:</strong> {doc.uploadDate}</p>
            </div>
            <div className="document-actions">
              <button
                onClick={() => handleEdit(doc)}
                className="edit-button"
              >
                Edit
              </button>

              <EditDocumentModal
                isOpen={isEditModalOpen}
                document={editDoc}
                onClose={() => setIsEditModalOpen(false)}
                onSave={handleSaveEdit}
                />
              {isAdmin && (
                <button
                  onClick={() => handleDelete(doc.id)}
                  className="delete-button"
                >
                  Delete
                </button>
              )}
            </div>
          </div>
        ))}

        {documents.length === 0 && !loading && (
          <p className="no-documents">No documents found.</p>
        )}
      </div>
    </div>
  );
};

export default DocumentsPage;