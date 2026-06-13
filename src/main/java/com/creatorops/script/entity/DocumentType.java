package com.creatorops.script.entity;

/**
 * <h3>Why this class exists</h3>
 * {@code DocumentType} enumerates the different script document strategies supported in CreatorOps.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Classifies whether a script's draft is edited internally via the basic rich-text editor or
 * linked/uploaded as an external reference (Google Doc, Microsoft Word Online, or uploaded file).
 */
public enum DocumentType {
    INTERNAL,
    GOOGLE_DOC,
    MS_WORD,
    UPLOADED_FILE
}
