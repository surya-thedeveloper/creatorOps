package com.creatorops.content.dto;

import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code ContentRequest} serves as the request DTO (Data Transfer Object) for content card operations.
 * It enforces validation rules on incoming JSON payloads.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It belongs in {@code com.creatorops.content.dto} to group data transfer structures.
 * <p>
 * <h3>Key Annotations</h3>
 * <ul>
 *   <li>{@code @NotBlank}: Enforces that titles are not null and contain at least one non-whitespace character.</li>
 *   <li>{@code @NotNull}: Enforces mandatory parameters like brand ownership and lifecycle stages.</li>
 *   <li>{@code @Size}: Constraints descriptions and titles to prevent overflow errors.</li>
 * </ul>
 * <p>
 * <h3>Design Decisions</h3>
 * Implementing DTOs as Java {@code record} classes guarantees immutability, thread-safety, and concise code syntax.
 */
public record ContentRequest(
    @NotNull(message = "Brand ID is required")
    Long brandId,

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    String description,

    @NotNull(message = "Content type is required")
    ContentType type,

    @NotNull(message = "Stage is required")
    ContentStage stage,

    @NotNull(message = "Priority is required")
    ContentPriority priority,

    OffsetDateTime dueDate,

    OffsetDateTime publishDate
) {}
