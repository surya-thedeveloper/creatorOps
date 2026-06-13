package com.creatorops.research.entity;

import com.creatorops.auth.entity.User;
import com.creatorops.common.entity.BaseEntity;
import com.creatorops.content.entity.Content;
import jakarta.persistence.*;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItem} is the JPA entity representing a singular research card linked to a content outline card.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Marks it as a persistent JPA class.</li>
 *   <li>{@code @Table(name = "research_item")}: Maps it to the SQL table named {@code research_item}.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)}: Serializes the enum type as standard text strings.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * <ul>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} Content: Establishes a lazy relationship. Research items are always queried in the context of their content card, so lazy loading prevents memory bloat.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} User: Associates the item to the user contributor who created it.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Research items collect references, observations, and outlines (Version 1 AI brainstorm logs) under a Content card during the initial workflow stages.
 */
@Entity
@Table(name = "research_item")
public class ResearchItem extends BaseEntity {

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
    @Column(name = "type", nullable = false)
    private ResearchItemType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String contentText;

    @Column(name = "url", length = 1024)
    private String url;

    public ResearchItem() {}

    public ResearchItem(Content content, User user, ResearchItemType type, String title, String contentText, String url) {
        this.content = content;
        this.user = user;
        this.type = type;
        this.title = title;
        this.contentText = contentText;
        this.url = url;
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

    public ResearchItemType getType() {
        return type;
    }

    public void setType(ResearchItemType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getContentId() {
        return content != null ? content.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}
