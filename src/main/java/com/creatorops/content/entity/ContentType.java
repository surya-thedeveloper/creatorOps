package com.creatorops.content.entity;

/**
 * <h3>Why this class exists</h3>
 * The {@code ContentType} enum classifies the specific publishing format of a Content item.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It is placed in {@code com.creatorops.content.entity} because it is a fundamental domain parameter 
 * and value boundary governing Content entity attributes.
 * <p>
 * <h3>Key Annotations</h3>
 * Standard Java Enum; mapped in JPA using {@code @Enumerated(EnumType.STRING)}.
 * <p>
 * <h3>Design Decisions</h3>
 * Enforcing specific types as an enum restricts user input to predefined valid platforms/mediums,
 * preventing data fragmentation (e.g. "YouTube Video" vs "youtube_video").
 */
public enum ContentType {
    YOUTUBE_VIDEO,
    REEL,
    SHORT,
    BLOG,
    LINKEDIN_POST,
    PODCAST,
    OTHER
}
