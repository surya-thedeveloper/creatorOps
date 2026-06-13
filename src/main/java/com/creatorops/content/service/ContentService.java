package com.creatorops.content.service;

import com.creatorops.content.dto.ContentRequest;
import com.creatorops.content.dto.ContentResponse;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <h3>Why this class exists</h3>
 * {@code ContentService} specifies the transactional business contract for Content management workflows.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * Placed in {@code com.creatorops.content.service} to isolate interface declarations from implementations.
 * <p>
 * <h3>Design Decisions</h3>
 * Defines method signatures that accept the active user's email to perform security/tenant verification.
 */
public interface ContentService {
    ContentResponse createContent(String currentUserEmail, ContentRequest request);
    ContentResponse getContentById(Long id, String currentUserEmail);
    Page<ContentResponse> searchContents(String currentUserEmail, Long brandId, ContentStage stage, ContentType type, String title, Pageable pageable);
    ContentResponse updateContent(Long id, ContentRequest request, String currentUserEmail);
    void deleteContent(Long id, String currentUserEmail);
}
