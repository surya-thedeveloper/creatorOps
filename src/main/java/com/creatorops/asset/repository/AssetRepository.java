package com.creatorops.asset.repository;

import com.creatorops.asset.entity.Asset;
import com.creatorops.asset.entity.AssetSource;
import com.creatorops.asset.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for {@link Asset} entity.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Finds assets associated with a content card.
     */
    Page<Asset> findByContentId(Long contentId, Pageable pageable);

    /**
     * Finds assets associated with a content card, filtering optionally by type and source.
     */
    @Query("SELECT a FROM Asset a WHERE a.content.id = :contentId " +
           "AND (:assetType IS NULL OR a.assetType = :assetType) " +
           "AND (:assetSource IS NULL OR a.assetSource = :assetSource)")
    Page<Asset> findByContentIdAndFilters(
            @Param("contentId") Long contentId,
            @Param("assetType") AssetType assetType,
            @Param("assetSource") AssetSource assetSource,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.content.brand.organization.id = :organizationId")
    long countByOrganizationId(@Param("organizationId") Long organizationId);
}

