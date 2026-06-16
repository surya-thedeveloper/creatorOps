package com.creatorops.assignment.controller;

import com.creatorops.assignment.dto.AssignmentRequest;
import com.creatorops.assignment.dto.AssignmentResponse;
import com.creatorops.assignment.dto.AssignmentStatusRequest;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.service.AssignmentService;
import com.creatorops.common.response.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentController} exposes REST endpoints under {@code /api} for managing task allocations.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @RestController}: Registers the class as a Spring REST controller.</li>
 *   <li>{@code @RequestMapping("/api")}: Scopes endpoints standard routing prefixes.</li>
 *   <li>{@code @Valid}: Enforces validation interceptors on payload request bounds.</li>
 * </ul>
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Routes HTTP allocations, status changes, lists, and deletions to transactional services.
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/contents/{contentId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @PathVariable Long contentId,
            Authentication authentication,
            @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(contentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @PathVariable Long id,
            Authentication authentication) {
        AssignmentResponse response = assignmentService.getAssignmentById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contents/{contentId}/assignments")
    public ResponseEntity<PagedResponse<AssignmentResponse>> getAssignmentsByContent(
            @PathVariable Long contentId,
            Authentication authentication,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getAssignmentsByContent(contentId, authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/assignments/my")
    public ResponseEntity<PagedResponse<AssignmentResponse>> getMyAssignments(
            @RequestParam(required = false) AssignmentStatus status,
            Authentication authentication,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getMyAssignments(authentication.getName(), status, pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @PutMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request,
            Authentication authentication) {
        AssignmentResponse response = assignmentService.updateAssignment(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/assignments/{id}/status")
    public ResponseEntity<AssignmentResponse> updateAssignmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentStatusRequest request,
            Authentication authentication) {
        AssignmentResponse response = assignmentService.updateAssignmentStatus(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        assignmentService.deleteAssignment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
