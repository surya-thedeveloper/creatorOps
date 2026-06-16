package com.creatorops.analytics.dto;

/**
 * <h3>PublishingAnalyticsResponse</h3>
 * DTO record representing the publication pipeline statistics across weekly and monthly boundaries.
 * <p>
 * <h3>How aggregate queries work</h3>
 * Counts are calculated using start and end boundaries calculated dynamically via server local/UTC offset datetimes,
 * then executing optimized database JPQL range queries.
 */
public record PublishingAnalyticsResponse(
    long publishedThisWeek,
    long publishedThisMonth,
    long upcomingThisWeek,
    long upcomingThisMonth,
    long scheduledThisWeek,
    long scheduledThisMonth
) {}
