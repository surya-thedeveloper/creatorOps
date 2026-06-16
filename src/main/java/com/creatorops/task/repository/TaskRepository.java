package com.creatorops.task.repository;

import com.creatorops.task.entity.Task;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for {@link Task} entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds tasks associated with an assignment.
     */
    Page<Task> findByAssignmentId(Long assignmentId, Pageable pageable);

    /**
     * Finds tasks assigned to a specific user, filtering optionally by status and priority.
     */
    @Query("SELECT t FROM Task t WHERE t.assignedToUser.id = :userId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByAssignedToUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.content.brand.organization.id = :organizationId")
    long countByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.content.brand.organization.id = :organizationId AND t.status = com.creatorops.task.entity.TaskStatus.DONE")
    long countCompletedByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.content.brand.organization.id = :organizationId AND t.dueDate < :now AND t.status != com.creatorops.task.entity.TaskStatus.DONE")
    long countOverdueByOrganizationId(@Param("organizationId") Long organizationId, @Param("now") java.time.OffsetDateTime now);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignment.content.brand.organization.id = :organizationId GROUP BY t.status")
    java.util.List<Object[]> countByStatus(@Param("organizationId") Long organizationId);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.assignment.content.brand.organization.id = :organizationId GROUP BY t.priority")
    java.util.List<Object[]> countByPriority(@Param("organizationId") Long organizationId);
}
