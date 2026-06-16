package com.creatorops.asset.entity;

import com.creatorops.auth.entity.User;
import com.creatorops.common.entity.BaseEntity;
import com.creatorops.content.entity.Content;
import jakarta.persistence.*;

/**
 * <h3>Why this class exists</h3>
 * {@code Asset} represents the metadata of a physical resource, video, audio track, thumbnail,
 * document, or external reference link associated with planned production content.
 * <p>
 * <h3>Why assets belong to content</h3>
 * A Content card acts as the central folder or repository of information for a planned media item.
 * Directing assets to belong directly to Content binds execution outputs to their parent card, keeping
 * the planning-to-delivery workflow fully cohesive.
 * <p>
 * <h3>Why versioning was implemented</h3>
 * Creative execution is iterative. Thumbnails, video edits, and scripts go through multiple reviews and revisions.
 * Storing a simple integer version number tracks incremental progress (e.g. Thumbnail V1 vs Thumbnail V2)
 * without requiring a complex Git-style version system.
 * <p>
 * <h3>How the module supports creator production workflows</h3>
 * Provides creators with a unified checklist and collection hub for all production files, facilitating collaboration
 * between writers, editors, and designers while ensuring strict tenant isolation and role permissions.
 */
@Entity
@Table(name = "asset")
public class Asset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_source", nullable = false, length = 50)
    private AssetSource assetSource;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_url", nullable = false, length = 2048)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    public Asset() {}

    public Asset(Content content, User uploadedByUser, AssetType assetType, AssetSource assetSource,
                 String name, String description, String fileUrl, Long fileSize, String mimeType, Integer version) {
        this.content = content;
        this.uploadedByUser = uploadedByUser;
        this.assetType = assetType;
        this.assetSource = assetSource;
        this.name = name;
        this.description = description;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.version = version != null ? version : 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public User getUploadedByUser() {
        return uploadedByUser;
    }

    public void setUploadedByUser(User uploadedByUser) {
        this.uploadedByUser = uploadedByUser;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public AssetSource getAssetSource() {
        return assetSource;
    }

    public void setAssetSource(AssetSource assetSource) {
        this.assetSource = assetSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
