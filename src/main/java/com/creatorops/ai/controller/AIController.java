package com.creatorops.ai.controller;

import com.creatorops.ai.service.AIService;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.script.dto.ScriptResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h3>AIController</h3>
 * Exposes REST endpoints to trigger AI brainstorming recommendations and script drafting processes.
 * Access is authorized for all core tenant roles (ADMIN, MANAGER, and CONTRIBUTOR), with
 * tenant isolation validated in the service layer.
 */
@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class AIController {

    private final AIService aiService;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /api/ai/contents/{contentId}/brainstorm
     * Generates a click hooks, visual themes, audience questions, and structure outline brainstorming report.
     * Results are stored in the ResearchItem module.
     */
    @PostMapping("/contents/{contentId}/brainstorm")
    public ResponseEntity<ResearchItemResponse> generateBrainstorm(
            @PathVariable Long contentId,
            Authentication authentication) {
        ResearchItemResponse response = aiService.generateBrainstorm(contentId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/contents/{contentId}/generate-script
     * Generates a conversational script draft and versions it under the Script module.
     */
    @PostMapping("/contents/{contentId}/generate-script")
    public ResponseEntity<ScriptResponse> generateScript(
            @PathVariable Long contentId,
            Authentication authentication) {
        ScriptResponse response = aiService.generateScript(contentId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
