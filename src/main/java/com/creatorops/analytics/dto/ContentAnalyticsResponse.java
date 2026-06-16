package com.creatorops.analytics.dto;

import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentPriority;
import java.util.Map;

/**
 * <h3>ContentAnalyticsResponse</h3>
 * DTO record containing grouped count metrics for Content cards.
 * <p>
 * <h3>Why analytics is projection-based</h3>
 * Using direct projections avoids keeping duplicate counts, ensuring the UI always displays up-to-the-second details.
 * <p>
 * <h3>How aggregate queries work</h3>
 * JPQL queries like {@code SELECT c.stage, COUNT(c) FROM Content c GROUP BY c.stage} execute efficiently
 * on the database index, returning only matching enum keys and their associated frequencies directly to the service.
 */
public record ContentAnalyticsResponse(
    Map<ContentStage, Long> contentByStage,
    Map<ContentType, Long> contentByType,
    Map<ContentPriority, Long> contentByPriority
) {}
