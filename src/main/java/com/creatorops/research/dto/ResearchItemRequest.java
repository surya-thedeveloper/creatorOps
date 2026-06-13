package com.creatorops.research.dto;

import com.creatorops.research.entity.ResearchItemType;
import jakarta.validation.constraints.Size;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemRequest} wraps incoming HTTP payloads for research item creations and updates.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @ResearchItemValid}: Class-level validator executing type-specific parameter checks.</li>
 *   <li>{@code @Size}: Restricts title and content string lengths to match database constraints.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * Declared as a Java {@code record} for thread-safe immutability.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Gathers user-submitted research values from Ember.js client screens.
 */
@ResearchItemValid
public record ResearchItemRequest(
    ResearchItemType type,

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Size(max = 10000, message = "Content cannot exceed 10000 characters")
    String content,

    @Size(max = 1024, message = "External URL cannot exceed 1024 characters")
    String externalUrl
) {}
