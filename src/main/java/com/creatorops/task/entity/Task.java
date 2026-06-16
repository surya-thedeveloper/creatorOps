package com.creatorops.task.entity;

import com.creatorops.assignment.entity.Assignment;
import com.creatorops.auth.entity.User;
import com.creatorops.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code Task} represents a physical checklist task belonging to an assignment context,
 * tracking titles, descriptions, priorities, due dates, and completion status.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * Resides in {@code com.creatorops.task.entity} to separate persistence models from transfer and service logic.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Marks this class as a JPA entity.</li>
 *   <li>{@code @Table(name = "task")}: Maps the entity to the physical SQL table.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)}: Serializes the Task enums as strings in the DB.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * <ul>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} Assignment: Lazy loading parents prevents join degradation during retrieval.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} assignedToUser: Maps the user responsible for completing the task.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} createdByUser: Tracks the creator of the task.</li>
 * </ul>
 */
@Entity
@Table(name = "task")
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public Task() {}

    public Task(Assignment assignment, String title, String description, TaskStatus status, TaskPriority priority, User assignedToUser, User createdByUser, OffsetDateTime dueDate) {
        this.assignment = assignment;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TaskStatus.TODO;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.assignedToUser = assignedToUser;
        this.createdByUser = createdByUser;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public User getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(User assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
