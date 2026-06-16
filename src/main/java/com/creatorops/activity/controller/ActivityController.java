package com.creatorops.activity.controller;

import com.creatorops.activity.dto.ActivityResponse;
import com.creatorops.activity.service.ActivityService;
import com.creatorops.common.response.PagedResponse;
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
    public ResponseEntity<ActivityResponse> getActivityById(
            @PathVariable Long id,
            Authentication authentication) {
        ActivityResponse response = activityService.getActivityById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
