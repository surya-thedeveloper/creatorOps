package com.creatorops.task.service;

import com.creatorops.task.dto.TaskRequest;
import com.creatorops.task.dto.TaskResponse;
import com.creatorops.task.dto.TaskStatusRequest;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Task operations.
 */
public interface TaskService {

    TaskResponse createTask(Long assignmentId, String userEmail, TaskRequest request);

    TaskResponse getTaskById(Long id, String userEmail);

    Page<TaskResponse> getTasksByAssignment(Long assignmentId, String userEmail, Pageable pageable);

    Page<TaskResponse> getMyTasks(String userEmail, TaskStatus status, TaskPriority priority, Pageable pageable);

    TaskResponse updateTask(Long id, String userEmail, TaskRequest request);

    TaskResponse updateTaskStatus(Long id, String userEmail, TaskStatusRequest request);

    void deleteTask(Long id, String userEmail);
}
