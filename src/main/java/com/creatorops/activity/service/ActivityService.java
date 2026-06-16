package com.creatorops.activity.service;

import com.creatorops.activity.dto.ActivityResponse;
import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import com.creatorops.auth.entity.User;
import com.creatorops.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for logging and retrieving chronological activity history records.
 */
public interface ActivityService {

    /**
     * Records a new activity in the timeline database.
     */
    void record(Content content, User user, EventType eventType, EntityType entityType, Long entityId, String description, String metadataJson);

    /**
     * Retrieves a single activity log detail by ID, checking tenant context.
     */
    ActivityResponse getActivityById(Long id, String userEmail);

    /**
     * Retrieves paginated activities associated with a content card, checking tenant context.
     */
    Page<ActivityResponse> getActivitiesByContent(Long contentId, String userEmail, Pageable pageable);
}
