package com.creatorops.calendar.controller;

import com.creatorops.calendar.dto.CalendarItemResponse;
import com.creatorops.calendar.service.CalendarService;
import com.creatorops.common.response.PagedResponse;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST controller exposing Content Calendar endpoints.
 */
@RestController
@RequestMapping("/api/calendar")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<List<CalendarItemResponse>> getCalendarRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ContentType contentType,
            @RequestParam(required = false) ContentStage stage,
            Authentication authentication) {
        List<CalendarItemResponse> response = calendarService.getCalendarRange(
                authentication.getName(), startDate, endDate, brandId, contentType, stage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<CalendarItemResponse>> getUpcomingContent(
            Authentication authentication,
            Pageable pageable) {
        Page<CalendarItemResponse> page = calendarService.getUpcomingContent(authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<PagedResponse<CalendarItemResponse>> getScheduledContent(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ContentType contentType,
            Authentication authentication,
            Pageable pageable) {
        Page<CalendarItemResponse> page = calendarService.getScheduledContent(
                authentication.getName(), brandId, contentType, pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/published")
    public ResponseEntity<PagedResponse<CalendarItemResponse>> getPublishedContent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            Authentication authentication,
            Pageable pageable) {
        Page<CalendarItemResponse> page = calendarService.getPublishedContent(
                authentication.getName(), startDate, endDate, pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<CalendarItemResponse>> getOverdueContent(
            Authentication authentication) {
        List<CalendarItemResponse> response = calendarService.getOverdueContent(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
