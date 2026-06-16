package com.creatorops.assignment.entity;

import com.creatorops.auth.entity.User;
import com.creatorops.common.entity.BaseEntity;
import com.creatorops.content.entity.Content;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code Assignment} represents the mapping association linking content cards to responsible creators.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Marks this class as a JPA entity.</li>
 *   <li>{@code @Table(name = "assignment")}: Binds the entity to the physical database table.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)}: Serializes the enum parameters as plain text.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * <ul>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} Content: Establishes a lazy relationship. Assignments are loaded in the context of their content card, so lazy fetching avoids memory overhead.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} assignedToUser: Associates the task assignee.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} assignedByUser: Associates the task assigner.</li>
 * </ul>
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Maps specific stages (e.g. EDITING, SCRIPT writing) to individual contributors, providing clear timelines and status states.
 */
@Entity
@Table(name = "assignment")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false)
    private AssignmentType assignmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "notes")
    private String notes;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public Assignment() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public User getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(User assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public User getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(User assignedByUser) {
        this.assignedByUser = assignedByUser;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getContentId() {
        return content != null ? content.getId() : null;
    }
}
