package com.creatorops.script.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h3>Why this class exists</h3>
 * {@code @ScriptValid} is a custom JSR-303 validation annotation applied to {@link ScriptRequest}
 * to enforce conditional validation checks based on the document type.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Constraint(validatedBy = ScriptValidator.class)}: Maps the validation contract to its implementation.</li>
 *   <li>{@code @Target(ElementType.TYPE)}: Scopes this annotation to class/record boundaries.</li>
 *   <li>{@code @Retention(RetentionPolicy.RUNTIME)}: Ensures runtime processing by Jakarta validation components.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Guarantees that internal scripts contain content text, and external options point to valid locations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScriptValidator.class)
public @interface ScriptValid {
    String message() default "Invalid script fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
