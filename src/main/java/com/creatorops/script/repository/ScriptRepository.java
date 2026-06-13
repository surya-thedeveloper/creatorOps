package com.creatorops.script.repository;

import com.creatorops.script.entity.Script;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptRepository} abstracts SQL queries against {@link Script} entities.
 * <p>
 * <h3>Chosen Annotations</h3>
 * {@code @Repository}: Registers it as a Spring Data component.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Provides queries to list script versions descending by version number and determine version counts for auto-increment logic.
 */
@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

    Page<Script> findByContentIdOrderByVersionDesc(Long contentId, Pageable pageable);

    @Query("SELECT COALESCE(MAX(s.version), 0) FROM Script s WHERE s.content.id = :contentId")
    int findMaxVersionByContentId(@Param("contentId") Long contentId);
}
