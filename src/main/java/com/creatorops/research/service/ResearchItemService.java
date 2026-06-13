package com.creatorops.research.service;

import com.creatorops.research.dto.ResearchItemRequest;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemService} specifies the business rules and operations for research card management.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Orchestrates creating notes/links/brainstorms, pagination retrievals, updating properties, and deletions under content cards.
 */
public interface ResearchItemService {
    ResearchItemResponse createResearchItem(Long contentId, String userEmail, ResearchItemRequest request);
    ResearchItemResponse getResearchItemById(Long id, String userEmail);
    Page<ResearchItemResponse> getResearchItemsByContent(Long contentId, ResearchItemType type, String userEmail, Pageable pageable);
    ResearchItemResponse updateResearchItem(Long id, ResearchItemRequest request, String userEmail);
    void deleteResearchItem(Long id, String userEmail);
}
