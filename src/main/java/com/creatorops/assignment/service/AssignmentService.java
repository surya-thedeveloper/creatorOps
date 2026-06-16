package com.creatorops.assignment.service;

import com.creatorops.assignment.dto.AssignmentRequest;
import com.creatorops.assignment.dto.AssignmentResponse;
import com.creatorops.assignment.dto.AssignmentStatusRequest;
import com.creatorops.assignment.entity.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentService} maps the business requirements for content assignment operations.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Orchestrates creating, viewing, status changes, and deletion of work assignments.
 */
public interface AssignmentService {
    AssignmentResponse createAssignment(Long contentId, String userEmail, AssignmentRequest request);
    AssignmentResponse getAssignmentById(Long id, String userEmail);
    Page<AssignmentResponse> getAssignmentsByContent(Long contentId, String userEmail, Pageable pageable);
    Page<AssignmentResponse> getMyAssignments(String userEmail, AssignmentStatus status, Pageable pageable);
    AssignmentResponse updateAssignment(Long id, String userEmail, AssignmentRequest request);
    AssignmentResponse updateAssignmentStatus(Long id, String userEmail, AssignmentStatusRequest request);
    void deleteAssignment(Long id, String userEmail);
}
