package com.creatorops.assignment.service;

import com.creatorops.assignment.dto.AssignmentRequest;
import com.creatorops.assignment.dto.AssignmentResponse;
import com.creatorops.assignment.dto.AssignmentStatusRequest;
import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentStatus;
import com.creatorops.assignment.repository.AssignmentRepository;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
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
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentServiceImpl} evaluates role permissions, checks organization tenant limits,
 * validates timeline due dates, and manages task ownership state transitions.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Service}: Registers this class as a Spring Service bean.</li>
 *   <li>{@code @Transactional}: Wraps writes in transactions and marks reads as read-only.</li>
 * </ul>
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Enforces security gates so only administrators/managers allocate tasks, while allowing creators
 * to self-report execution statuses (e.g. IN_PROGRESS, BLOCKED, COMPLETED).
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 ContentRepository contentRepository,
                                 UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AssignmentResponse createAssignment(Long contentId, String userEmail, AssignmentRequest request) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC: Only ADMIN or MANAGER can create assignments
        if (creator.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage assignments.");
        }

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary for content card
        if (!content.getBrand().getOrganizationId().equals(creator.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        User assignedUser = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedToUserId()));

        // Enforce tenant boundary for assigned user
        if (!assignedUser.getOrganizationId().equals(creator.getOrganizationId())) {
            throw new IllegalArgumentException("Validation failed: Assigned user must belong to the same organization.");
        }

        // Validate due date: Must not be in the past
        if (request.dueDate() != null && request.dueDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Validation failed: Due date cannot be in the past.");
        }

        Assignment assignment = new Assignment();
        assignment.setContent(content);
        assignment.setAssignedToUser(assignedUser);
        assignment.setAssignedByUser(creator);
        assignment.setAssignmentType(request.assignmentType());
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setNotes(request.notes());
        assignment.setDueDate(request.dueDate());

        Assignment saved = assignmentRepository.save(assignment);
        return AssignmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        // Enforce tenant boundary
        if (!assignment.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        return AssignmentResponse.fromEntity(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAssignmentsByContent(Long contentId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        Page<Assignment> assignments = assignmentRepository.findByContentId(contentId, pageable);
        return assignments.map(AssignmentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getMyAssignments(String userEmail, AssignmentStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Assignment> assignments;
        if (status != null) {
            assignments = assignmentRepository.findByAssignedToUserIdAndStatus(user.getId(), status, pageable);
        } else {
            assignments = assignmentRepository.findByAssignedToUserId(user.getId(), pageable);
        }

        return assignments.map(AssignmentResponse::fromEntity);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(Long id, String userEmail, AssignmentRequest request) {
        User updater = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC
        if (updater.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage assignments.");
        }

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        // Enforce tenant boundary for existing assignment
        if (!assignment.getContent().getBrand().getOrganizationId().equals(updater.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        User assignedUser = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedToUserId()));

        // Enforce tenant boundary for assigned user
        if (!assignedUser.getOrganizationId().equals(updater.getOrganizationId())) {
            throw new IllegalArgumentException("Validation failed: Assigned user must belong to the same organization.");
        }

        // Validate due date: Must not be in the past
        if (request.dueDate() != null && request.dueDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Validation failed: Due date cannot be in the past.");
        }

        assignment.setAssignedToUser(assignedUser);
        assignment.setAssignmentType(request.assignmentType());
        assignment.setNotes(request.notes());
        assignment.setDueDate(request.dueDate());

        Assignment updated = assignmentRepository.save(assignment);
        return AssignmentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignmentStatus(Long id, String userEmail, AssignmentStatusRequest request) {
        User updater = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        // Enforce tenant boundary
        if (!assignment.getContent().getBrand().getOrganizationId().equals(updater.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        // Enforce RBAC for status updates:
        // Contributors can only update their own assignments
        if (updater.getRole() == UserRole.CONTRIBUTOR && !assignment.getAssignedToUser().getId().equals(updater.getId())) {
            throw new AccessDeniedException("Access denied: Contributors can only update the status of assignments assigned to themselves.");
        }

        AssignmentStatus oldStatus = assignment.getStatus();
        AssignmentStatus newStatus = request.status();

        assignment.setStatus(newStatus);

        // Manage lifecycle dates: startedAt / completedAt
        if (newStatus == AssignmentStatus.IN_PROGRESS && assignment.getStartedAt() == null) {
            assignment.setStartedAt(OffsetDateTime.now());
        }

        if (newStatus == AssignmentStatus.COMPLETED) {
            if (assignment.getStartedAt() == null) {
                assignment.setStartedAt(OffsetDateTime.now());
            }
            assignment.setCompletedAt(OffsetDateTime.now());
        } else if (oldStatus == AssignmentStatus.COMPLETED && newStatus != AssignmentStatus.COMPLETED) {
            // Re-opened task: clear completed date
            assignment.setCompletedAt(null);
        }

        Assignment updated = assignmentRepository.save(assignment);
        return AssignmentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id, String userEmail) {
        User deleter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC
        if (deleter.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage assignments.");
        }

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        // Enforce tenant boundary
        if (!assignment.getContent().getBrand().getOrganizationId().equals(deleter.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        assignmentRepository.delete(assignment);
    }

    /**
     * <h3>Future Extension Points Placeholder</h3>
     * This module serves as a base hook for collaboration triggers:
     * <ul>
     *   <li><b>Notifications</b>: Trigger automated in-app notifications/emails when tasks are assigned or blocked.</li>
     *   <li><b>Reminders & SLAs</b>: Set scheduled cron runs evaluating approaching `dueDate` limits against completion status.</li>
     *   <li><b>Workload Balancing</b>: Build capacity planning dashboard counts showing active tasks grouped by user ID.</li>
     *   <li><b>Assignment Analytics</b>: Compute average completion times comparing `startedAt` and `completedAt`.</li>
     * </ul>
     */
    public void futureCollaborationExtensionsPlaceholder() {
        // Placeholders mapping extension points
    }
}
