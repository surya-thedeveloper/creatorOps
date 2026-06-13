package com.creatorops.content.entity;

/**
 * <h3>Why this class exists</h3>
 * The {@code ContentPriority} enum tracks the execution urgency of a content card.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It belongs in {@code com.creatorops.content.entity} as it defines priority boundaries for Content entity fields.
 * <p>
 * <h3>Key Annotations</h3>
 * Standard Java Enum; serialized as a String in the database.
 * <p>
 * <h3>Design Decisions</h3>
 * Enforcing urgency values as enums simplifies sorting and card prioritization on kanban board components.
 */
public enum ContentPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
