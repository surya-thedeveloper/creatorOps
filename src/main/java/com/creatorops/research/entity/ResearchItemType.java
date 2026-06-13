package com.creatorops.research.entity;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemType} classifies the specific format of research gathered by creator teams
 * (e.g. text notes, external references, or AI-generated brainstorming angles).
 * <p>
 * <h3>Chosen Annotations</h3>
 * Mapped to the database as a string using {@code @Enumerated(EnumType.STRING)} to avoid fragile ordinal mappings.
 * <p>
 * <h3>Relationship Design</h3>
 * Serves as a type attribute on the {@link ResearchItem} entity.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * In the Research stage, users group gathered information into Notes (observations, outlines),
 * Links ( competitor videos, articles), or AI Brainstorm logs (hooks, ideas). This enum governs how each item is validated and displayed.
 */
public enum ResearchItemType {
    NOTE,
    LINK,
    AI_BRAINSTORM
}
