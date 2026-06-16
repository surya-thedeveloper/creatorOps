package com.creatorops.activity.listener;

import com.creatorops.activity.service.ActivityService;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.event.DomainEvent;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for application domain events and records audit logs asynchronously.
 * Completely decouples business services from direct ActivityService dependencies.
 */
@Component
public class ActivityEventListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityEventListener.class);

    private final ActivityService activityService;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ActivityEventListener(ActivityService activityService,
                                 ContentRepository contentRepository,
                                 UserRepository userRepository) {
        this.activityService = activityService;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Asynchronously handle published domain events after transaction commit.
     */
    @Async("creatorOpsAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        log.debug("Processing domain event: id={}, type={}, entityId={}", 
                event.getEventId(), event.getEventType(), event.getEntityId());

        try {
            Content content = contentRepository.findById(event.getContentId()).orElse(null);
            User user = userRepository.findById(event.getUserId()).orElse(null);

            if (content == null) {
                log.warn("Content card with id {} not found for event {}", event.getContentId(), event.getEventId());
                return;
            }

            if (user == null) {
                log.warn("Performer User with id {} not found for event {}", event.getUserId(), event.getEventId());
                return;
            }

            activityService.record(
                content,
                user,
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getDescription(),
                event.getMetadataJson()
            );

            log.debug("Activity logged successfully for event {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to log activity details for event {}", event.getEventId(), e);
            throw e; // Will be captured by the AsyncExceptionHandler
        }
    }
}
