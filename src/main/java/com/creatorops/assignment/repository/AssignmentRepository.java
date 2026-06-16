package com.creatorops.assignment.repository;

import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentRepository} abstracts database queries targeting {@link Assignment} records.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Yields query capabilities sorting content assignments or extracting specific dashboard status profiles for contributors.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByContentId(Long contentId, Pageable pageable);

    Page<Assignment> findByAssignedToUserId(Long userId, Pageable pageable);

    Page<Assignment> findByAssignedToUserIdAndStatus(Long userId, AssignmentStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.content.brand.organization.id = :organizationId")
    long countByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.content.brand.organization.id = :organizationId AND a.status IN (com.creatorops.assignment.entity.AssignmentStatus.ASSIGNED, com.creatorops.assignment.entity.AssignmentStatus.IN_PROGRESS, com.creatorops.assignment.entity.AssignmentStatus.BLOCKED)")
    long countActiveByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT a.status, COUNT(a) FROM Assignment a WHERE a.content.brand.organization.id = :organizationId GROUP BY a.status")
    java.util.List<Object[]> countByStatus(@Param("organizationId") Long organizationId);

    @Query("SELECT a.assignmentType, COUNT(a) FROM Assignment a WHERE a.content.brand.organization.id = :organizationId GROUP BY a.assignmentType")
    java.util.List<Object[]> countByType(@Param("organizationId") Long organizationId);
}
