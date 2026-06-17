package com.creatorops.activity.controller;

import com.creatorops.activity.dto.ActivityResponse;
import com.creatorops.activity.service.ActivityService;
import com.creatorops.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for retrieving activity logs.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Activity Timeline", description = "Read-only audit log of all events within a content card's lifecycle.")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final ActivityService activityService;

    @Autowired
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Retrieves paginated activities associated with a content card, sorted by newest first by default.
     */
    @GetMapping("/contents/{contentId}/activities")
    @Operation(summary = "List activity timeline for a content card", description = "Paginated, sorted newest first by default. Scoped to caller's organization.")
    @ApiResponse(responseCode = "200", description = "Activity timeline returned")
    public ResponseEntity<PagedResponse<ActivityResponse>> getActivitiesByContent(
            @PathVariable Long contentId,
            Authentication authentication,
            Pageable pageable) {

        // If no sort is provided, default to newest first (createdAt, desc)
        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        Page<ActivityResponse> page = activityService.getActivitiesByContent(contentId, authentication.getName(), sortedPageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    /**
     * Retrieves detail information for a single activity log by ID.
     */
    @GetMapping("/activities/{id}")
    @Operation(summary = "Get activity log entry by ID")
    @ApiResponse(responseCode = "200", description = "Activity entry returned")
    public ResponseEntity<ActivityResponse> getActivityById(
            @PathVariable Long id,
            Authentication authentication) {
        ActivityResponse response = activityService.getActivityById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
