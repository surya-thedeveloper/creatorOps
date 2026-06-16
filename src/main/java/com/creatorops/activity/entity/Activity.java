package com.creatorops.activity.entity;

import com.creatorops.auth.entity.User;
import com.creatorops.content.entity.Content;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code Activity} represents a chronological audit trail/historical log of events occurring on content cards.
 * <p>
 * <h3>Why activities are immutable</h3>
 * Activities represent concrete, immutable historical facts. Once logged, they cannot be modified or deleted via API
 * to guarantee audit integrity. Consequently, this class does not extend {@link com.creatorops.common.entity.BaseEntity}
 * (which handles lastModified dates) and only tracks a single {@code createdAt} timestamp.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Marks this class as a JPA entity.</li>
 *   <li>{@code @Table(name = "activity")}: Maps it to the SQL table {@code activity}.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)}: Serializes enums to text columns.</li>
 * </ul>
 */
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "metadata_json")
    private String metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Activity() {}

    public Activity(Content content, User user, EventType eventType, EntityType entityType, Long entityId, String description, String metadataJson) {
        this.content = content;
        this.user = user;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.metadataJson = metadataJson;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
