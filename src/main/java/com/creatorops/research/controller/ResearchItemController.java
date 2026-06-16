package com.creatorops.research.controller;

import com.creatorops.common.response.PagedResponse;
import com.creatorops.research.dto.ResearchItemRequest;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.research.service.ResearchItemService;
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
 * {@code ResearchItemController} exposes REST endpoints under {@code /api} for managing research cards.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @RestController}: Registers the class as an HTTP REST endpoint container.</li>
 *   <li>{@code @RequestMapping}: Scopes mappings cleanly under standard API path groups.</li>
 *   <li>{@code @Valid}: Enforces Jakarta Bean Validation on the request payload.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Routes actions from creator workspace UI controls (saving link references, text observations, or brainstorm parameters)
 * directly to the underlying transactional service layers.
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class ResearchItemController {

    private final ResearchItemService researchItemService;

    @Autowired
    public ResearchItemController(ResearchItemService researchItemService) {
        this.researchItemService = researchItemService;
    }

    @PostMapping("/contents/{contentId}/research")
    public ResponseEntity<ResearchItemResponse> createResearchItem(
            @PathVariable Long contentId,
            Authentication authentication,
            @Valid @RequestBody ResearchItemRequest request) {
        ResearchItemResponse response = researchItemService.createResearchItem(contentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/research/{id}")
    public ResponseEntity<ResearchItemResponse> getResearchItemById(
            @PathVariable Long id,
            Authentication authentication) {
        ResearchItemResponse response = researchItemService.getResearchItemById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contents/{contentId}/research")
    public ResponseEntity<PagedResponse<ResearchItemResponse>> getResearchItemsByContent(
            @PathVariable Long contentId,
            @RequestParam(required = false) ResearchItemType type,
            Authentication authentication,
            Pageable pageable) {
        Page<ResearchItemResponse> page = researchItemService.getResearchItemsByContent(contentId, type, authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @PutMapping("/research/{id}")
    public ResponseEntity<ResearchItemResponse> updateResearchItem(
            @PathVariable Long id,
            @Valid @RequestBody ResearchItemRequest request,
            Authentication authentication) {
        ResearchItemResponse response = researchItemService.updateResearchItem(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/research/{id}")
    public ResponseEntity<Void> deleteResearchItem(
            @PathVariable Long id,
            Authentication authentication) {
        researchItemService.deleteResearchItem(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
