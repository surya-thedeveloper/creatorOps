package com.creatorops.script.dto;

import com.creatorops.script.entity.DocumentType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptValidator} executes dynamic field validation checking based on the request's {@link DocumentType}.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Ensures script payload requests meet business validation criteria before reaching databases:
 * <ul>
 *   <li>{@code INTERNAL} document type requires {@code editorContent}.</li>
 *   <li>{@code GOOGLE_DOC} & {@code MS_WORD} document types require {@code externalDocumentUrl}.</li>
 *   <li>{@code UPLOADED_FILE} document type requires {@code uploadedFileReference}.</li>
 * </ul>
 */
public class ScriptValidator implements ConstraintValidator<ScriptValid, ScriptRequest> {

    @Override
    public boolean isValid(ScriptRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (request.documentType() == null) {
            context.buildConstraintViolationWithTemplate("Document type is required")
                   .addPropertyNode("documentType")
                   .addConstraintViolation();
            return false;
        }

        switch (request.documentType()) {
            case INTERNAL:
                if (request.editorContent() == null || request.editorContent().isBlank()) {
                    context.buildConstraintViolationWithTemplate("Editor content is required for document type INTERNAL")
                           .addPropertyNode("editorContent")
                           .addConstraintViolation();
                    isValid = false;
                }
                break;
            case GOOGLE_DOC:
            case MS_WORD:
                if (request.externalDocumentUrl() == null || request.externalDocumentUrl().isBlank()) {
                    context.buildConstraintViolationWithTemplate("External document URL is required for document type " + request.documentType())
                           .addPropertyNode("externalDocumentUrl")
                           .addConstraintViolation();
                    isValid = false;
                }
                break;
            case UPLOADED_FILE:
                if (request.uploadedFileReference() == null || request.uploadedFileReference().isBlank()) {
                    context.buildConstraintViolationWithTemplate("Uploaded file reference is required for document type UPLOADED_FILE")
                           .addPropertyNode("uploadedFileReference")
                           .addConstraintViolation();
                    isValid = false;
                }
                break;
        }

        return isValid;
    }
}
