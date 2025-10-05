package com.studystart.jyu.models;

import java.util.Locale;

public enum DocumentType {
  PASSPORT,
  RP_CARD,
  ACCEPTANCE_LETTER,
  TRANSCRIPT;

  public static DocumentType fromString(String raw) {
    if (raw == null) return null;
    String s = raw.trim().toUpperCase(Locale.ROOT)
                  .replace('-', '_')
                  .replace(' ', '_');
    switch (s) {
      case "PASSPORT": return PASSPORT;
      case "RP_CARD":
      case "RP":
      case "RESIDENCE_PERMIT":
      case "RESIDENCE_PERMIT_CARD": return RP_CARD;
      case "ACCEPTANCE_LETTER":
      case "UNIVERSITY_ACCEPTANCE":
      case "UNI_ACCEPTANCE":
      case "OFFER_LETTER": return ACCEPTANCE_LETTER;
      case "TRANSCRIPT": return TRANSCRIPT;
      default:
        throw new IllegalArgumentException("Unsupported documentType: " + raw +
          " (allowed: PASSPORT, RP_CARD, ACCEPTANCE_LETTER)");
    }
  }
}

