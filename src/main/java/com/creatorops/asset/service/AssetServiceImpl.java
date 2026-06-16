package com.creatorops.asset.service;

import com.creatorops.activity.entity.EntityType;
import com.creatorops.activity.entity.EventType;
import com.creatorops.activity.service.ActivityService;
import com.creatorops.asset.dto.AssetRequest;
import com.creatorops.asset.dto.AssetResponse;
import com.creatorops.asset.entity.Asset;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import com.creatorops.asset.repository.AssetRepository;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h3>Why this class exists</h3>
 * {@code AssetServiceImpl} executes core business logic, validations, role permission checks,
 * and timeline integration log recordings for CreatorOps production assets.
 * <p>
 * <h3>Why assets belong to content</h3>
 * Binds the media output references directly to Content planning cards, keeping the workflow pipeline unified.
 * <p>
 * <h3>Why versioning was implemented</h3>
 * Tracks creative iteration (V1, V2 edits) transparently to support content revision loops.
 * <p>
 * <h3>How the module supports creator workflows</h3>
 * Helps team roles (designers, editors, copywriters) organize assets in one place under tenant security barriers.
 */
@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Autowired
    public AssetServiceImpl(AssetRepository assetRepository,
                            ContentRepository contentRepository,
                            UserRepository userRepository,
                            ActivityService activityService) {
        this.assetRepository = assetRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @Override
    @Transactional
    public AssetResponse createAsset(Long contentId, String userEmail, AssetRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary: content's brand organization must match user's organization
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        Asset asset = new Asset(
            content,
            user,
            request.assetType(),
            request.assetSource(),
            request.name(),
            request.description(),
            request.fileUrl(),
            request.fileSize(),
            request.mimeType(),
            request.version()
        );

        Asset saved = assetRepository.save(asset);

        // Record in timeline
        activityService.record(
            content,
            user,
            EventType.ASSET_CREATED,
            EntityType.ASSET,
            saved.getId(),
            "Asset '" + saved.getName() + "' was added",
            null
        );

        return AssetResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        // Enforce tenant boundary
        if (!asset.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Asset belongs to a different organization.");
        }

        return AssetResponse.fromEntity(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssetResponse> getAssetsByContent(Long contentId, String userEmail, AssetType assetType, AssetSource assetSource, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        Page<Asset> assets = assetRepository.findByContentIdAndFilters(contentId, assetType, assetSource, pageable);
        return assets.map(AssetResponse::fromEntity);
    }

    @Override
    @Transactional
    public AssetResponse updateAsset(Long id, String userEmail, AssetRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        // Enforce tenant boundary
        if (!asset.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Asset belongs to a different organization.");
        }

        // Enforce RBAC permissions:
        // Contributors can only modify assets they uploaded.
        if (user.getRole() == UserRole.CONTRIBUTOR && !asset.getUploadedByUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied: Contributors can only modify assets they created.");
        }

        asset.setName(request.name());
        asset.setDescription(request.description());
        asset.setAssetType(request.assetType());
        asset.setAssetSource(request.assetSource());
        asset.setFileUrl(request.fileUrl());
        asset.setFileSize(request.fileSize());
        asset.setMimeType(request.mimeType());
        if (request.version() != null) {
            asset.setVersion(request.version());
        }

        Asset updated = assetRepository.save(asset);

        // Record in timeline
        activityService.record(
            updated.getContent(),
            user,
            EventType.ASSET_UPDATED,
            EntityType.ASSET,
            updated.getId(),
            "Asset '" + updated.getName() + "' was updated",
            null
        );

        return AssetResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteAsset(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        // Enforce tenant boundary
        if (!asset.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Asset belongs to a different organization.");
        }

        // Enforce RBAC permissions:
        // Contributors can only delete assets they uploaded.
        if (user.getRole() == UserRole.CONTRIBUTOR && !asset.getUploadedByUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied: Contributors can only delete assets they created.");
        }

        // Record in timeline before physically deleting
        activityService.record(
            asset.getContent(),
            user,
            EventType.ASSET_DELETED,
            EntityType.ASSET,
            asset.getId(),
            "Asset '" + asset.getName() + "' was deleted",
            null
        );

        assetRepository.delete(asset);
    }

    /**
     * <h3>Future Extension Points Placeholder</h3>
     * This module serves as a base hook for asset integrations:
     * <ul>
     *   <li><b>Google Drive Integration</b>: Synchronize folders and pull raw file metadata directly via Google Drive API.</li>
     *   <li><b>OneDrive Integration</b>: Bind OneDrive folder pointers to automated upload cards.</li>
     *   <li><b>Direct File Uploads & S3</b>: Stream binary objects directly to cloud object storage (e.g. AWS S3) and store signed CDN keys.</li>
     *   <li><b>Asset Previews</b>: Render responsive video thumbnails or image overlays in-app using transcoding gateways.</li>
     *   <li><b>Asset Approval Workflow</b>: Enforce manager review stage blocks where a draft asset cannot be marked as publish-ready until signed off.</li>
     * </ul>
     */
    public void futureAssetExtensionsPlaceholder() {
        // Placeholders mapping extension points
    }
}
