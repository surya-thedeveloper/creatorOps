package com.creatorops.script.dto;

import com.creatorops.script.entity.Script;
import com.creatorops.script.entity.DocumentType;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptResponse} formats persistent script database models into clean HTTP output JSON bodies.
 * <p>
 * <h3>Relationship Design</h3>
 * Uses a static factory mapping {@code fromEntity} to transform JPA classes to records.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Returns script details, version indicators, and external links back to UI pages in creator workspaces.
 */
public record ScriptResponse(
    Long id,
    Long contentId,
    Integer version,
    String generatedScript,
    String editorContent,
    DocumentType documentType,
    String externalDocumentUrl,
    String uploadedFileReference,
    Long createdById,
    String createdByName,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ScriptResponse fromEntity(Script script) {
        return new ScriptResponse(
            script.getId(),
            script.getContentId(),
            script.getVersion(),
            script.getGeneratedScript(),
            script.getEditorContent(),
            script.getDocumentType(),
            script.getExternalDocumentUrl(),
            script.getUploadedFileReference(),
            script.getUserId(),
            script.getUser() != null ? script.getUser().getName() : null,
            script.getCreatedAt(),
            script.getUpdatedAt()
        );
    }
}
