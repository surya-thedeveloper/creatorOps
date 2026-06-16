package com.creatorops.content.controller;

import com.creatorops.common.response.PagedResponse;
import com.creatorops.content.dto.ContentRequest;
import com.creatorops.content.dto.ContentResponse;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.service.ContentService;
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
 * {@code ContentController} exposes REST endpoints under {@code /api/contents} for client interaction,
 * mapping HTTP actions to content domain services.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * Resides in {@code com.creatorops.content.controller} to structure API routers adjacent to their specific subdomain context.
 * <p>
 * <h3>Key Annotations</h3>
 * <ul>
 *   <li>{@code @RestController}: Marks the class as an API controller, returning JSON directly.</li>
 *   <li>{@code @RequestMapping("/api/contents")}: Maps all endpoints in this controller under a base URL path.</li>
 *   <li>{@code @PreAuthorize}: Restricts write operations (POST, PUT, DELETE) to users holding {@code ADMIN} or {@code MANAGER} roles.</li>
 *   <li>{@code @Valid}: Triggers standard request DTO validations.</li>
 * </ul>
 * <p>
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li>Provides robust search parameters (brandId, stage, type, title search) which are fully optional.</li>
 *   <li>Delegates authentications directly via Spring Security {@code Authentication} parameter parsing.</li>
 *   <li>Utilizes standard REST status codes (201 Created for POST, 204 No Content for DELETE, 200 OK for GET/PUT).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/contents")
public class ContentController {

    private final ContentService contentService;

    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ContentResponse> createContent(
            Authentication authentication,
            @Valid @RequestBody ContentRequest request) {
        ContentResponse response = contentService.createContent(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getContentById(
            @PathVariable Long id,
            Authentication authentication) {
        ContentResponse response = contentService.getContentById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ContentResponse>> getContents(
            Authentication authentication,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ContentStage stage,
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) String title,
            Pageable pageable) {
        Page<ContentResponse> responsePage = contentService.searchContents(
            authentication.getName(),
            brandId,
            stage,
            type,
            title,
            pageable
        );
        return ResponseEntity.ok(PagedResponse.fromPage(responsePage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody ContentRequest request,
            Authentication authentication) {
        ContentResponse response = contentService.updateContent(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long id,
            Authentication authentication) {
        contentService.deleteContent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
