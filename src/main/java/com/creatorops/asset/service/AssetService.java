package com.creatorops.asset.service;

import com.creatorops.asset.dto.AssetRequest;
import com.creatorops.asset.dto.AssetResponse;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Asset management operations.
 */
public interface AssetService {

    AssetResponse createAsset(Long contentId, String userEmail, AssetRequest request);

    AssetResponse getAssetById(Long id, String userEmail);

    Page<AssetResponse> getAssetsByContent(Long contentId, String userEmail, AssetType assetType, AssetSource assetSource, Pageable pageable);

    AssetResponse updateAsset(Long id, String userEmail, AssetRequest request);

    void deleteAsset(Long id, String userEmail);
}
