package com.creatorops.research.dto;

import com.creatorops.research.entity.ResearchItemType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemValidator} evaluates the conditional validity of a {@link ResearchItemRequest} DTO payload.
 * <p>
 * <h3>Chosen Annotations</h3>
 * Standard implementation of {@code ConstraintValidator}.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Prevents corrupted research records from reaching the database, validating field constraints:
 * <ul>
 *   <li>{@code title} must not be blank for all cards.</li>
 *   <li>{@code content} must not be blank for {@code NOTE} and {@code AI_BRAINSTORM} cards.</li>
 *   <li>{@code externalUrl} must not be blank for {@code LINK} reference cards.</li>
 * </ul>
 */
public class ResearchItemValidator implements ConstraintValidator<ResearchItemValid, ResearchItemRequest> {

    @Override
    public boolean isValid(ResearchItemRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (request.title() == null || request.title().isBlank()) {
            context.buildConstraintViolationWithTemplate("Title is required")
                   .addPropertyNode("title")
                   .addConstraintViolation();
            isValid = false;
        }

        if (request.type() == null) {
            context.buildConstraintViolationWithTemplate("Research item type is required")
                   .addPropertyNode("type")
                   .addConstraintViolation();
            return false;
        }

        if (request.type() == ResearchItemType.NOTE || request.type() == ResearchItemType.AI_BRAINSTORM) {
            if (request.content() == null || request.content().isBlank()) {
                context.buildConstraintViolationWithTemplate("Content is required for type " + request.type())
                       .addPropertyNode("content")
                       .addConstraintViolation();
                isValid = false;
            }
        } else if (request.type() == ResearchItemType.LINK) {
            if (request.externalUrl() == null || request.externalUrl().isBlank()) {
                context.buildConstraintViolationWithTemplate("External URL is required for type LINK")
                       .addPropertyNode("externalUrl")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
