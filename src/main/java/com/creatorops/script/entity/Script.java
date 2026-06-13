package com.creatorops.script.entity;

import com.creatorops.auth.entity.User;
import com.creatorops.common.entity.BaseEntity;
import com.creatorops.content.entity.Content;
import jakarta.persistence.*;

/**
 * <h3>Why this class exists</h3>
 * {@code Script} represents a single script version linked to a Content planning card.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Entity}: Registers this class as a persistent JPA entity.</li>
 *   <li>{@code @Table(name = "script")}: Maps this entity to the SQL table {@code script}.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)}: Serializes the enum type as standard text strings.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * <ul>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} Content: Establishes a lazy relationship. Scripts are fetched in the context of their content card, so lazy loading minimizes memory overhead.</li>
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} User: Links this script version to the user who wrote/saved it.</li>
 * </ul>
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Tracks the actual draft text and pointers to external document resources (Google Docs URLs)
 * as the Content lifecycle moves from Research to SCRIPT stage.
 */
@Entity
@Table(name = "script")
public class Script extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "generated_script")
    private String generatedScript;

    @Column(name = "editor_content")
    private String editorContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "external_document_url", length = 1024)
    private String externalDocumentUrl;

    @Column(name = "uploaded_file_reference", length = 1024)
    private String uploadedFileReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Script() {}

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getGeneratedScript() {
        return generatedScript;
    }

    public void setGeneratedScript(String generatedScript) {
        this.generatedScript = generatedScript;
    }

    public String getEditorContent() {
        return editorContent;
    }

    public void setEditorContent(String editorContent) {
        this.editorContent = editorContent;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getExternalDocumentUrl() {
        return externalDocumentUrl;
    }

    public void setExternalDocumentUrl(String externalDocumentUrl) {
        this.externalDocumentUrl = externalDocumentUrl;
    }

    public String getUploadedFileReference() {
        return uploadedFileReference;
    }

    public void setUploadedFileReference(String uploadedFileReference) {
        this.uploadedFileReference = uploadedFileReference;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getContentId() {
        return content != null ? content.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}
