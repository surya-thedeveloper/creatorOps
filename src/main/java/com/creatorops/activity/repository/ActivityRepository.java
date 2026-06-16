package com.creatorops.activity.repository;

import com.creatorops.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for {@link Activity} entity.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Finds activities associated with a content ID, returned within a pageable envelope.
     */
    Page<Activity> findByContentId(Long contentId, Pageable pageable);
}
