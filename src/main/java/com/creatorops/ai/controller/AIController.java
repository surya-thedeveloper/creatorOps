package com.creatorops.ai.controller;

import com.creatorops.ai.service.AIService;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.script.dto.ScriptResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/ai")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
@Tag(name = "AI", description = "AI-powered brainstorming and script generation. Rate-limited to 5 calls/minute per user. Requires GEMINI_API_KEY to be configured.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
        summary = "Generate brainstorm ideas",
        description = "Uses AI to generate click hooks, visual themes, audience questions, and structure outlines based on the content's research. Result is saved as an AI_BRAINSTORM research item."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Brainstorm generated and stored"),
        @ApiResponse(responseCode = "404", description = "Content not found"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded (5 calls/min per user)"),
        @ApiResponse(responseCode = "502", description = "AI provider error")
    })
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
    @Operation(
        summary = "Generate script draft",
        description = "Uses AI to draft a conversational script based on content metadata and research notes. Script is versioned and stored in the Script module."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Script generated and stored"),
        @ApiResponse(responseCode = "404", description = "Content not found"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded (5 calls/min per user)"),
        @ApiResponse(responseCode = "502", description = "AI provider error")
    })
    public ResponseEntity<ScriptResponse> generateScript(
            @PathVariable Long contentId,
            Authentication authentication) {
        ScriptResponse response = aiService.generateScript(contentId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
