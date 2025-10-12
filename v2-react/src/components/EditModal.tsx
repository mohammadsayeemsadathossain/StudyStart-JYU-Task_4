import React, { useState, useEffect } from "react";

interface EditDocumentModalProps {
  isOpen: boolean;
  document: {
    id: number;
    documentType: string;
    fileName: string;
  } | null;
  onClose: () => void;
  onSave: (docId: number, updatedDoc: { documentType: string; fileName: string }) => void;
}

const EditDocumentModal: React.FC<EditDocumentModalProps> = ({ isOpen, document, onClose, onSave }) => {
  const [docType, setDocType] = useState(document?.documentType || "");
  const [fileName, setFileName] = useState(document?.fileName || "");

  useEffect(() => {
    if (document) {
      setDocType(document.documentType);
      setFileName(document.fileName);
    }
  }, [document]);

  if (!isOpen || !document) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(document.id, { documentType: docType, fileName });
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2>Edit Document</h2>
        <form onSubmit={handleSubmit}>
          <label>Document Type</label>
          <select value={docType} onChange={(e) => setDocType(e.target.value)}>
            <option value="PASSPORT">Passport</option>
            <option value="RP_CARD">Residence Permit</option>
            <option value="ACCEPTANCE_LETTER">Acceptance Letter</option>
          </select>

          <label>File Name</label>
          <input value={fileName} onChange={(e) => setFileName(e.target.value)} />

          <div className="modal-actions">
            <button type="button" onClick={onClose}>Cancel</button>
            <button type="submit">Save</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditDocumentModal;
