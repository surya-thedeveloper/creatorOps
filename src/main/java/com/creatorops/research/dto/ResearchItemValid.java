package com.creatorops.research.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h3>Why this class exists</h3>
 * {@code @ResearchItemValid} is a custom JSR-303 annotation applied to {@link ResearchItemRequest} to enforce
 * conditional validation rules depending on the selected research type (NOTE, LINK, or AI_BRAINSTORM).
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Constraint(validatedBy = ResearchItemValidator.class)}: Binds this annotation to its concrete validation logic.</li>
 *   <li>{@code @Target(ElementType.TYPE)}: Restricts this annotation to class/record boundaries.</li>
 *   <li>{@code @Retention(RetentionPolicy.RUNTIME)}: Retains the constraint information at execution runtime.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * Integrates with standard Jakarta Bean Validation API, feeding failures directly to Spring's {@code MethodArgumentNotValidException} mapping.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Ensures that a LINK type reference contains an URL, while a NOTE or AI_BRAINSTORM outlines contain actual content text before they are saved.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResearchItemValidator.class)
public @interface ResearchItemValid {
    String message() default "Invalid research item fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
