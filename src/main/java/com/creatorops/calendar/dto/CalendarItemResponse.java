package com.creatorops.calendar.dto;

import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import java.time.OffsetDateTime;

/**
 * DTO record representing a single item projection in the Content Calendar.
 */
public record CalendarItemResponse(
    Long contentId,
    String title,
    Long brandId,
    String brandName,
    ContentType contentType,
    ContentStage stage,
    OffsetDateTime publishDate,
    OffsetDateTime dueDate,
    ContentPriority priority
) {
    public static CalendarItemResponse fromEntity(Content content) {
        return new CalendarItemResponse(
            content.getId(),
            content.getTitle(),
            content.getBrand().getId(),
            content.getBrand().getName(),
            content.getType(),
            content.getStage(),
            content.getPublishDate(),
            content.getDueDate(),
            content.getPriority()
        );
    }
}
