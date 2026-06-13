package com.creatorops.content.entity;

/**
 * <h3>Why this class exists</h3>
 * The {@code ContentStage} enum maps the chronological steps of a content item through the creation pipeline.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It resides in {@code com.creatorops.content.entity} as it represents an attribute of the domain entity.
 * <p>
 * <h3>Key Annotations</h3>
 * Standard Java Enum; serialized as a String in the database.
 * <p>
 * <h3>Design Decisions</h3>
 * Storing lifecycle states as enums ensures type-safety. For V1, the application stores and allows updates
 * to stages without workflow transition rules, which can be easily added as custom service validations in future updates.
 */
public enum ContentStage {
    IDEA,
    RESEARCH,
    SCRIPT,
    PRODUCTION,
    EDITING,
    REVIEW,
    SCHEDULED,
    PUBLISHED,
    ON_HOLD,
    CANCELLED
}
