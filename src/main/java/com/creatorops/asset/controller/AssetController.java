package com.creatorops.asset.controller;

import com.creatorops.asset.dto.AssetRequest;
import com.creatorops.asset.dto.AssetResponse;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import com.creatorops.asset.service.AssetService;
import com.creatorops.common.response.PagedResponse;
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
 * REST controller exposing Asset management endpoints.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CONTRIBUTOR')")
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/contents/{contentId}/assets")
    public ResponseEntity<AssetResponse> createAsset(
            @PathVariable Long contentId,
            Authentication authentication,
            @Valid @RequestBody AssetRequest request) {
        AssetResponse response = assetService.createAsset(contentId, authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable Long id,
            Authentication authentication) {
        AssetResponse response = assetService.getAssetById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contents/{contentId}/assets")
    public ResponseEntity<PagedResponse<AssetResponse>> getAssetsByContent(
            @PathVariable Long contentId,
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(required = false) AssetSource assetSource,
            Authentication authentication,
            Pageable pageable) {
        Page<AssetResponse> page = assetService.getAssetsByContent(contentId, authentication.getName(), assetType, assetSource, pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(page));
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<AssetResponse> updateAsset(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody AssetRequest request) {
        AssetResponse response = assetService.updateAsset(id, authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> deleteAsset(
            @PathVariable Long id,
            Authentication authentication) {
        assetService.deleteAsset(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
