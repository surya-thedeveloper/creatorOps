package com.creatorops.task.service;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import com.creatorops.activity.service.ActivityService;
import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.repository.AssignmentRepository;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.task.dto.TaskRequest;
import com.creatorops.task.dto.TaskResponse;
import com.creatorops.task.dto.TaskStatusRequest;
import com.creatorops.task.entity.Task;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code TaskServiceImpl} implements business validation, role-based access checks,
 * due date logic, and activity timeline logging for all task checklist cards.
 * <p>
 * <h3>Why tasks belong to assignments</h3>
 * While Assignments represent ownership boundaries (e.g. SCRIPT writer), Tasks represent individual units
 * of execution. Mapping tasks under assignments allows managers to track detailed work progress while maintaining
 * a clean workflow owner model.
 * <p>
 * <h3>Why status tracking was designed this way</h3>
 * Transitioning a task status to {@link TaskStatus#DONE} automatically stamps the `completedAt` timestamp,
 * providing accurate performance metrics. Returning the task to a non-done state safely resets this timestamp.
 * <p>
 * <h3>How it supports creator workflows</h3>
 * Prevents contributors from deleting or modifying tasks assigned to others, while giving them a clean
 * dashboard queue for self-reporting execution status.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           AssignmentRepository assignmentRepository,
                           UserRepository userRepository,
                           ActivityService activityService) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long assignmentId, String userEmail, TaskRequest request) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC: Only ADMIN or MANAGER can create tasks
        if (creator.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage tasks.");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Enforce tenant boundary: Assignment content must belong to same organization
        if (!assignment.getContent().getBrand().getOrganizationId().equals(creator.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        User assignedUser = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedToUserId()));

        // Enforce tenant boundary: Assigned user must belong to same organization
        if (!assignedUser.getOrganizationId().equals(creator.getOrganizationId())) {
            throw new IllegalArgumentException("Validation failed: Assigned user must belong to the same organization.");
        }

        // Validate due date
        if (request.dueDate() != null && request.dueDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Validation failed: Due date cannot be in the past.");
        }

        Task task = new Task(
            assignment,
            request.title(),
            request.description(),
            TaskStatus.TODO,
            request.priority(),
            assignedUser,
            creator,
            request.dueDate()
        );

        Task saved = taskRepository.save(task);
        org.slf4j.MDC.put("entityId", String.valueOf(saved.getId()));
        log.info("Created task: title={}, assignmentId={}", saved.getTitle(), saved.getAssignment().getId());
        org.slf4j.MDC.remove("entityId");

        // Record in timeline
        activityService.record(
            assignment.getContent(),
            creator,
            EventType.TASK_CREATED,
            EntityType.TASK,
            saved.getId(),
            "Task '" + saved.getTitle() + "' was created",
            null
        );

        return TaskResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Enforce tenant boundary
        if (!task.getAssignment().getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Task belongs to a different organization.");
        }

        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByAssignment(Long assignmentId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        // Enforce tenant boundary
        if (!assignment.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Assignment belongs to a different organization.");
        }

        Page<Task> tasks = taskRepository.findByAssignmentId(assignmentId, pageable);
        return tasks.map(TaskResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getMyTasks(String userEmail, TaskStatus status, TaskPriority priority, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Task> tasks = taskRepository.findByAssignedToUserIdAndFilters(user.getId(), status, priority, pageable);
        return tasks.map(TaskResponse::fromEntity);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, String userEmail, TaskRequest request) {
        User updater = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC: Only ADMIN or MANAGER can modify tasks
        if (updater.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage tasks.");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Enforce tenant boundary
        if (!task.getAssignment().getContent().getBrand().getOrganizationId().equals(updater.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Task belongs to a different organization.");
        }

        User assignedUser = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id: " + request.assignedToUserId()));

        // Enforce tenant boundary
        if (!assignedUser.getOrganizationId().equals(updater.getOrganizationId())) {
            throw new IllegalArgumentException("Validation failed: Assigned user must belong to the same organization.");
        }

        // Validate due date
        if (request.dueDate() != null && request.dueDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Validation failed: Due date cannot be in the past.");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setAssignedToUser(assignedUser);
        task.setDueDate(request.dueDate());

        Task updated = taskRepository.save(task);
        org.slf4j.MDC.put("entityId", String.valueOf(updated.getId()));
        log.info("Updated task: title={}, priority={}, assignedToUserId={}", updated.getTitle(), updated.getPriority(), updated.getAssignedToUser().getId());
        org.slf4j.MDC.remove("entityId");

        // Record in timeline
        activityService.record(
            updated.getAssignment().getContent(),
            updater,
            EventType.TASK_UPDATED,
            EntityType.TASK,
            updated.getId(),
            "Task '" + updated.getTitle() + "' was updated",
            null
        );

        return TaskResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long id, String userEmail, TaskStatusRequest request) {
        User updater = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Enforce tenant boundary
        if (!task.getAssignment().getContent().getBrand().getOrganizationId().equals(updater.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Task belongs to a different organization.");
        }

        // Enforce RBAC: Contributor can only update status of tasks assigned to themselves
        if (updater.getRole() == UserRole.CONTRIBUTOR && !task.getAssignedToUser().getId().equals(updater.getId())) {
            throw new AccessDeniedException("Access denied: Contributors can only update status of tasks assigned to themselves.");
        }

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.status();

        task.setStatus(newStatus);

        // Completion timestamp logic
        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(OffsetDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        Task updated = taskRepository.save(task);
        org.slf4j.MDC.put("entityId", String.valueOf(updated.getId()));
        log.info("Updated task status: oldStatus={}, newStatus={}", oldStatus, newStatus);
        org.slf4j.MDC.remove("entityId");

        // Record in timeline
        activityService.record(
            updated.getAssignment().getContent(),
            updater,
            EventType.TASK_STATUS_CHANGED,
            EntityType.TASK,
            updated.getId(),
            "Task status changed from " + oldStatus + " to " + newStatus,
            "{\"oldStatus\":\"" + oldStatus + "\",\"newStatus\":\"" + newStatus + "\"}"
        );

        return TaskResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteTask(Long id, String userEmail) {
        User deleter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce RBAC: Only ADMIN or MANAGER can delete tasks
        if (deleter.getRole() == UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Only ADMIN or MANAGER can manage tasks.");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Enforce tenant boundary
        if (!task.getAssignment().getContent().getBrand().getOrganizationId().equals(deleter.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Task belongs to a different organization.");
        }

        // Record in timeline before physically deleting
        activityService.record(
            task.getAssignment().getContent(),
            deleter,
            EventType.TASK_DELETED,
            EntityType.TASK,
            task.getId(),
            "Task '" + task.getTitle() + "' was deleted",
            null
        );

        org.slf4j.MDC.put("entityId", String.valueOf(task.getId()));
        log.info("Deleted task: taskId={}", task.getId());
        org.slf4j.MDC.remove("entityId");

        taskRepository.delete(task);
    }

    /**
     * <h3>Future Extension Points Placeholder</h3>
     * This module acts as the foundation for granular task-level collaboration and execution tracking.
     * The following extension points are designed to be integrated without altering core schemas:
     * <ul>
     *   <li><b>Task Comments</b>: A task-level comment system enabling discussion thread cards on individual sub-tasks.</li>
     *   <li><b>Task Attachments</b>: Linking reference files or generated image assets specifically to a task checklist card.</li>
     *   <li><b>Task Reminders</b>: Automated push notification alerts or emails triggered as task due dates approach.</li>
     *   <li><b>Task Dependencies</b>: Enforcing execution sequences where Task B cannot move from TODO to IN_PROGRESS until Task A is DONE.</li>
     *   <li><b>Recurring Tasks</b>: Supporting scheduled/automated task checklist generation based on content templates or recurring calendar patterns.</li>
     * </ul>
     */
    public void futureTaskExtensionsPlaceholder() {
        // Placeholders mapping future extension points
    }
}
