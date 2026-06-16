package com.creatorops.task.controller;

import com.creatorops.common.response.PagedResponse;
import com.creatorops.task.dto.TaskRequest;
import com.creatorops.task.dto.TaskResponse;
import com.creatorops.task.dto.TaskStatusRequest;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.service.TaskService;
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
 * REST controller exposing Task endpoints.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/assignments/{assignmentId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long assignmentId,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(assignmentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            Authentication authentication) {
        TaskResponse response = taskService.getTaskById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{assignmentId}/tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> getTasksByAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication,
            Pageable pageable) {
        Page<TaskResponse> page = taskService.getTasksByAssignment(assignmentId, authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/tasks/my")
    public ResponseEntity<PagedResponse<TaskResponse>> getMyTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Authentication authentication,
            Pageable pageable) {
        Page<TaskResponse> page = taskService.getMyTasks(authentication.getName(), status, priority, pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody TaskStatusRequest request) {
        TaskResponse response = taskService.updateTaskStatus(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
