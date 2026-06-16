package com.creatorops.content.entity;

import com.creatorops.brand.entity.Brand;
import com.creatorops.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * <h3>Why this class exists</h3>
 * The {@code Content} class is the central JPA entity representing a content card in the system.
 * It tracks workflow stage, format type, scheduling, and references the owner Brand.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It belongs in {@code com.creatorops.content.entity} to separate domain models from services, DTOs, and controllers.
 * <p>
 * <h3>Key Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Marks this class as a JPA entity.</li>
 *   <li>{@code @Table(name = "content")}: Maps it to the SQL table named {@code content}.</li>
 *   <li>{@code @SQLDelete}: Overrides the default physical delete query to perform a soft-delete update.</li>
 *   <li>{@code @SQLRestriction}: Automates filtering to exclude soft-deleted records from JPA queries.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)}: Links to the parent Brand, preventing N+1 queries.</li>
 * </ul>
 * <p>
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li><b>Soft Deletion</b>: Implements soft deletion via {@code isDeleted} and {@code deletedAt} to prevent accidental loss of scripts.</li>
 *   <li><b>String Enums</b>: Serializes {@code ContentType}, {@code ContentStage}, and {@code ContentPriority} enums as text.</li>
 *   <li><b>Omit created_by</b>: Excluded from physical mappings to remain compliant with the V1 PostgreSQL DDL.</li>
 * </ul>
 */
@Entity
@Table(name = "content")
@SQLDelete(sql = "UPDATE content SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Content extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ContentStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ContentPriority priority = ContentPriority.MEDIUM;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "publish_date")
    private OffsetDateTime publishDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public Content() {}

    public Content(Brand brand, String title, String description, ContentType type, ContentStage stage, ContentPriority priority, OffsetDateTime dueDate, OffsetDateTime publishDate) {
        this.brand = brand;
        this.title = title;
        this.description = description;
        this.type = type;
        this.stage = stage;
        this.priority = priority != null ? priority : ContentPriority.MEDIUM;
        this.dueDate = dueDate;
        this.publishDate = publishDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public ContentStage getStage() {
        return stage;
    }

    public void setStage(ContentStage stage) {
        this.stage = stage;
    }

    public ContentPriority getPriority() {
        return priority;
    }

    public void setPriority(ContentPriority priority) {
        this.priority = priority;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public OffsetDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(OffsetDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Transient
    public Long getBrandId() {
        return brand != null ? brand.getId() : null;
    }
}
