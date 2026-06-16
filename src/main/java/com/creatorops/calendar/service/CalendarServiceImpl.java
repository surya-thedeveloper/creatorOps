package com.creatorops.calendar.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.calendar.dto.CalendarItemResponse;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h3>Why Calendar is a projection instead of a separate entity</h3>
 * A Content Calendar displays content events scheduled for publication or due soon.
 * Creating a separate calendar table would introduce redundancy, duplicate scheduling columns,
 * and data synchronization issues. Implementing it as a projection ensures that calendar queries
 * are fast and always perfectly consistent with the underlying content data.
 * <p>
 * <h3>Why Content remains the source of truth</h3>
 * All production workflows, assignments, tasks, and scripts are anchored to the {@link Content} card.
 * Keeping content as the single source of truth ensures that stage updates, date changes, and status shifts
 * propagate instantly to the calendar without requiring complex entity sync logic.
 * <p>
 * <h3>How filtering and scheduling work</h3>
 * Scheduling is driven by `publishDate` and `dueDate` parameters on Content. Filtering queries leverage
 * JPQL joining with Brand to restrict lookups to the caller's organization ID (tenant boundary enforcement)
 * while optimizing SQL executes using `JOIN FETCH` queries on brand relations.
 * <p>
 * <h3>How the module supports creator planning workflows</h3>
 * Helps managers and creators visualize content distribution, identify scheduling gaps, trace published history,
 * and highlight overdue items whose target due dates have passed.
 */
@Service
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public CalendarServiceImpl(ContentRepository contentRepository,
                               UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<CalendarItemResponse> getCalendarRange(
            String userEmail,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Long brandId,
            ContentType contentType,
            ContentStage stage) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Validation failed: Start date must be before or equal to end date.");
        }

        List<Content> events = contentRepository.findCalendarEvents(
                user.getOrganizationId(),
                startDate,
                endDate,
                brandId,
                contentType,
                stage
        );

        return events.stream()
                .map(CalendarItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CalendarItemResponse> getUpcomingContent(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Content> contents = contentRepository.findUpcomingContent(
                user.getOrganizationId(),
                OffsetDateTime.now(),
                pageable
        );

        return contents.map(CalendarItemResponse::fromEntity);
    }

    @Override
    public Page<CalendarItemResponse> getScheduledContent(
            String userEmail,
            Long brandId,
            ContentType contentType,
            Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Content> contents = contentRepository.findScheduledContent(
                user.getOrganizationId(),
                brandId,
                contentType,
                pageable
        );

        return contents.map(CalendarItemResponse::fromEntity);
    }

    @Override
    public Page<CalendarItemResponse> getPublishedContent(
            String userEmail,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Validation failed: Start date must be before or equal to end date.");
        }

        Page<Content> contents = contentRepository.findPublishedContent(
                user.getOrganizationId(),
                startDate,
                endDate,
                pageable
        );

        return contents.map(CalendarItemResponse::fromEntity);
    }

    @Override
    public List<CalendarItemResponse> getOverdueContent(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Content> contents = contentRepository.findOverdueContent(
                user.getOrganizationId(),
                OffsetDateTime.now()
        );

        return contents.stream()
                .map(CalendarItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * <h3>Future Frontend Support Placeholder</h3>
     * This module serves as the projection foundation for the following frontend calendar capabilities:
     * <ul>
     *   <li><b>Month View / Week View / Agenda View</b>: Map the range search endpoints to grid views, displaying contents on target dates.</li>
     *   <li><b>Brand & Channel Filters</b>: Slice and filter calendar view records dynamically in-browser.</li>
     *   <li><b>Drag-and-Drop Rescheduling</b>: Trigger a PATCH endpoint updating content `publishDate` or `dueDate` on card drops.</li>
     * </ul>
     */
    public void futureFrontendExtensionsPlaceholder() {
        // Placeholders mapping calendar extension points
    }
}
