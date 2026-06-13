package com.creatorops.research.repository;

import com.creatorops.research.entity.ResearchItem;
import com.creatorops.research.entity.ResearchItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemRepository} abstracts database queries targeting {@link ResearchItem} records.
 * <p>
 * <h3>Chosen Annotations</h3>
 * {@code @Repository}: Registers it as a Spring Data repository interface.
 * <p>
 * <h3>Relationship Design</h3>
 * Serves as the database interface for {@link ResearchItem}.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Provides paginated lists of gathered notes/links/brainstorms under a content planning card.
 */
@Repository
public interface ResearchItemRepository extends JpaRepository<ResearchItem, Long> {
    Page<ResearchItem> findByContentIdAndType(Long contentId, ResearchItemType type, Pageable pageable);
    Page<ResearchItem> findByContentId(Long contentId, Pageable pageable);
}
