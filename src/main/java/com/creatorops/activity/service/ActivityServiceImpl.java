package com.creatorops.activity.service;

import com.creatorops.activity.dto.ActivityResponse;
import com.creatorops.activity.entity.Activity;
import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import com.creatorops.activity.repository.ActivityRepository;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h3>Why this class exists</h3>
 * {@code ActivityServiceImpl} implements the central Activity log timeline actions,
 * persisting immutable audit logs and enforcing tenant security boundaries on retrieval.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Service}: Registers this class as a Spring Service bean.</li>
 *   <li>{@code @Transactional}: Scopes transaction boundaries. Writes are saved immediately, and reads are optimized.</li>
 * </ul>
 * <p>
 * <h3>Why a centralized ActivityService was chosen</h3>
 * Centralizing activity recording prevents logic duplication across content, research, script, and assignment modules,
 * and decouples the event recording mechanism from individual domain lifecycles.
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ActivityServiceImpl(ActivityRepository activityRepository,
                               ContentRepository contentRepository,
                               UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void record(Content content, User user, EventType eventType, EntityType entityType, Long entityId, String description, String metadataJson) {
        Activity activity = new Activity(content, user, eventType, entityType, entityId, description, metadataJson);
        activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        // Enforce tenant boundary: activity's content brand organization must match user's organization
        if (!activity.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Activity belongs to a different organization.");
        }

        return ActivityResponse.fromEntity(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getActivitiesByContent(Long contentId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        Page<Activity> activities = activityRepository.findByContentId(contentId, pageable);
        return activities.map(ActivityResponse::fromEntity);
    }
}
