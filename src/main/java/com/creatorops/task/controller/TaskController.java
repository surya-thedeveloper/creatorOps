package com.creatorops.task.controller;

import com.creatorops.common.response.PagedResponse;
import com.creatorops.task.dto.TaskRequest;
import com.creatorops.task.dto.TaskResponse;
import com.creatorops.task.dto.TaskStatusRequest;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import com.creatorops.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
@Tag(name = "Tasks", description = "Manage checklist tasks nested inside content assignments.")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/assignments/{assignmentId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create task", description = "Creates a checklist task under an assignment. ADMIN or MANAGER only.")
    @ApiResponse(responseCode = "201", description = "Task created")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long assignmentId,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(assignmentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task by ID")
    @ApiResponse(responseCode = "200", description = "Task returned")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            Authentication authentication) {
        TaskResponse response = taskService.getTaskById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{assignmentId}/tasks")
    @Operation(summary = "List tasks for an assignment")
    @ApiResponse(responseCode = "200", description = "Tasks returned")
    public ResponseEntity<PagedResponse<TaskResponse>> getTasksByAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication,
            Pageable pageable) {
        Page<TaskResponse> page = taskService.getTasksByAssignment(assignmentId, authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @GetMapping("/tasks/my")
    @Operation(summary = "List my tasks", description = "Returns tasks assigned to the authenticated user. Filter by status and priority.")
    @ApiResponse(responseCode = "200", description = "My tasks returned")
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
    @Operation(summary = "Update task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{id}/status")
    @Operation(summary = "Update task status", description = "Transitions task status: TODO → IN_PROGRESS → DONE.")
    @ApiResponse(responseCode = "200", description = "Task status updated")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody TaskStatusRequest request) {
        TaskResponse response = taskService.updateTaskStatus(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
