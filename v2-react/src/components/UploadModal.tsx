import React, { useState } from "react";
import "../styles/Documents.css";

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpload: (file: File, type: string) => void;
}

const UploadModal: React.FC<UploadModalProps> = ({ isOpen, onClose, onUpload }) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [selectedType, setSelectedType] = useState<string>("PASSPORT");

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedFile) {
      onUpload(selectedFile, selectedType);
      setSelectedFile(null);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h2 className="modal-title">Upload New Document</h2>

        <form onSubmit={handleSubmit}>
          <label className="modal-label">Document Type</label>
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="modal-select"
          >
            <option value="PASSPORT">Passport</option>
            <option value="RP_CARD">Residence Permit</option>
            <option value="ACCEPTANCE_LETTER">Acceptance Letter</option>
          </select>

          <label className="modal-label">Select File</label>
          <input
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
            className="modal-file-input"
          />

          <div className="modal-buttons">
            <button
              type="button"
              onClick={onClose}
              className="modal-btn cancel-btn"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!selectedFile}
              className="modal-btn upload-btn"
            >
              Upload
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UploadModal;
