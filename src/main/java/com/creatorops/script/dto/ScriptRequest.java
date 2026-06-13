package com.creatorops.script.dto;

import com.creatorops.script.entity.DocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptRequest} contains the incoming parameters needed to create or update a script version.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @ScriptValid}: Triggers custom type-conditional constraint validation.</li>
 *   <li>{@code @NotNull}: Enforces document type presence.</li>
 *   <li>{@code @Size}: Keeps payload character lengths bounded below database constraint properties.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Receives the text draft modifications, Google Docs URLs, or file pathways submitted by creators from the client interface.
 */
@ScriptValid
public record ScriptRequest(
    @NotNull(message = "Document type is required")
    DocumentType documentType,

    @Size(max = 100000, message = "Editor content cannot exceed 100000 characters")
    String editorContent,

    @Size(max = 1024, message = "External document URL cannot exceed 1024 characters")
    String externalDocumentUrl,

    @Size(max = 1024, message = "Uploaded file reference cannot exceed 1024 characters")
    String uploadedFileReference,

    @Size(max = 100000, message = "Generated script cannot exceed 100000 characters")
    String generatedScript
) {}
