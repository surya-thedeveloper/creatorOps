package com.creatorops.script.controller;

import com.creatorops.common.response.PagedResponse;
import com.creatorops.script.dto.ScriptRequest;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptController} exposes REST endpoints under {@code /api} for managing content scripts.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @RestController}: Registers the class as an HTTP endpoint handler.</li>
 *   <li>{@code @RequestMapping("/api")}: Scopes endpoints under the standard API prefix.</li>
 *   <li>{@code @Valid}: Activates request validation validation interceptors.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Receives actions from the user interface to save new script versions, update pointers, and list versions.
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
@Tag(name = "Scripts", description = "Manage script drafts and versions linked to content cards.")
@SecurityRequirement(name = "bearerAuth")
public class ScriptController {

    private final ScriptService scriptService;

    @Autowired
    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @PostMapping("/contents/{contentId}/scripts")
    @Operation(summary = "Create script", description = "Creates a new script draft for the given content card.")
    @ApiResponse(responseCode = "201", description = "Script created")
    public ResponseEntity<ScriptResponse> createScript(
            @PathVariable Long contentId,
            Authentication authentication,
            @Valid @RequestBody ScriptRequest request) {
        ScriptResponse response = scriptService.createScript(contentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/scripts/{id}")
    @Operation(summary = "Get script by ID")
    @ApiResponse(responseCode = "200", description = "Script returned")
    public ResponseEntity<ScriptResponse> getScriptById(
            @PathVariable Long id,
            Authentication authentication) {
        ScriptResponse response = scriptService.getScriptById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contents/{contentId}/scripts")
    @Operation(summary = "List scripts for a content card")
    @ApiResponse(responseCode = "200", description = "Scripts returned")
    public ResponseEntity<PagedResponse<ScriptResponse>> getScriptsByContent(
            @PathVariable Long contentId,
            Authentication authentication,
            Pageable pageable) {
        Page<ScriptResponse> page = scriptService.getScriptsByContent(contentId, authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @PutMapping("/scripts/{id}")
    @Operation(summary = "Update script content or metadata")
    @ApiResponse(responseCode = "200", description = "Script updated")
    public ResponseEntity<ScriptResponse> updateScript(
            @PathVariable Long id,
            @Valid @RequestBody ScriptRequest request,
            Authentication authentication) {
        ScriptResponse response = scriptService.updateScript(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/scripts/{id}")
    @Operation(summary = "Delete script")
    @ApiResponse(responseCode = "204", description = "Script deleted")
    public ResponseEntity<Void> deleteScript(
            @PathVariable Long id,
            Authentication authentication) {
        scriptService.deleteScript(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
