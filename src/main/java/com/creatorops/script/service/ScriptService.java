package com.creatorops.script.service;

import com.creatorops.script.dto.ScriptRequest;
import com.creatorops.script.dto.ScriptResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptService} lists the required business capabilities for script drafting workflows.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Handles creating script versions, retrieving details, paginating updates, and removing records.
 */
public interface ScriptService {
    ScriptResponse createScript(Long contentId, String userEmail, ScriptRequest request);
    ScriptResponse getScriptById(Long id, String userEmail);
    Page<ScriptResponse> getScriptsByContent(Long contentId, String userEmail, Pageable pageable);
    ScriptResponse updateScript(Long id, ScriptRequest request, String userEmail);
    void deleteScript(Long id, String userEmail);
}
