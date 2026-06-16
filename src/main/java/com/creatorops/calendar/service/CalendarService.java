package com.creatorops.calendar.service;

import com.creatorops.calendar.dto.CalendarItemResponse;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service interface for Content Calendar operations.
 */
public interface CalendarService {

    List<CalendarItemResponse> getCalendarRange(
            String userEmail,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Long brandId,
            ContentType contentType,
            ContentStage stage
    );

    Page<CalendarItemResponse> getUpcomingContent(String userEmail, Pageable pageable);

    Page<CalendarItemResponse> getScheduledContent(
            String userEmail,
            Long brandId,
            ContentType contentType,
            Pageable pageable
    );

    Page<CalendarItemResponse> getPublishedContent(
            String userEmail,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

    List<CalendarItemResponse> getOverdueContent(String userEmail);
}
