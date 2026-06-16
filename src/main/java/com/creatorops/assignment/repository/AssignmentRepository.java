package com.creatorops.assignment.repository;

import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
