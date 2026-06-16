package com.creatorops.content.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.brand.entity.Brand;
import com.creatorops.brand.repository.BrandRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.dto.ContentRequest;
import com.creatorops.content.dto.ContentResponse;
import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.activity.entity.EventType;
import com.creatorops.common.event.DomainEventPublisher;
import com.creatorops.common.event.ContentCreatedEvent;
import com.creatorops.common.event.ContentUpdatedEvent;
import com.creatorops.common.event.ContentDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

/**
 * <h3>Why this class exists</h3>
 * {@code ContentServiceImpl} implements the core business rules for creating, querying, updating,
 * and deleting content, ensuring strict tenant isolation checks.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * Resides in {@code com.creatorops.content.service} to keep service implementations logically decoupled from endpoints.
 * <p>
 * <h3>Key Annotations</h3>
 * <ul>
 *   <li>{@code @Service}: Registers this class as a Spring-managed service bean.</li>
 *   <li>{@code @Transactional}: Wraps public methods in database transactions. Read-only optimizations applied where appropriate.</li>
 *   <li>{@code @Autowired}: Injects dependency repositories.</li>
 * </ul>
 * <p>
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li><b>Tenant Isolation</b>: Prior to any content modification or detail query, the service retrieves the logged-in user's organization scope and checks it against the Brand's organization. If they mismatch, a Spring Security {@code AccessDeniedException} is thrown.</li>
 *   <li><b>Soft Deletion Cascade</b>: Delegating content deletion to {@code contentRepository.delete(content)} automatically triggers the Hibernate-configured soft delete sequence.</li>
 * </ul>
 */
@Service
public class ContentServiceImpl implements ContentService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Autowired
    public ContentServiceImpl(ContentRepository contentRepository,
                              BrandRepository brandRepository,
                              UserRepository userRepository,
                              DomainEventPublisher domainEventPublisher) {
        this.contentRepository = contentRepository;
        this.brandRepository = brandRepository;
        this.userRepository = userRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public ContentResponse createContent(String currentUserEmail, ContentRequest request) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == com.creatorops.auth.entity.UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Contributors cannot perform this action.");
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.brandId()));

        if (!brand.getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot create content for a brand outside your organization.");
        }

        Content content = new Content(
            brand,
            request.title(),
            request.description(),
            request.type(),
            request.stage(),
            request.priority(),
            request.dueDate(),
            request.publishDate()
        );

        Content saved = contentRepository.save(content);
        org.slf4j.MDC.put("entityId", String.valueOf(saved.getId()));
        log.info("Created content card: title={}, brandId={}, stage={}", saved.getTitle(), saved.getBrand().getId(), saved.getStage());
        org.slf4j.MDC.remove("entityId");
        domainEventPublisher.publish(new ContentCreatedEvent(
            user.getId(),
            user.getOrganizationId(),
            saved.getId(),
            saved.getTitle(),
            saved.getStage().name()
        ));

        if (saved.getStage() == ContentStage.SCHEDULED || saved.getPublishDate() != null) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                saved.getId(),
                saved.getTitle(),
                EventType.CONTENT_SCHEDULED,
                "Content '" + saved.getTitle() + "' was scheduled"
            ));
        } else if (saved.getStage() == ContentStage.PUBLISHED) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                saved.getId(),
                saved.getTitle(),
                EventType.CONTENT_PUBLISHED,
                "Content '" + saved.getTitle() + "' was published"
            ));
        }

        return ContentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponse getContentById(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot view content outside your organization.");
        }

        return ContentResponse.fromEntity(content);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentResponse> searchContents(String currentUserEmail, Long brandId, ContentStage stage, ContentType type, String title, Pageable pageable) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce tenant boundary: searchContents only queries items belonging to the user's organizationId
        Page<Content> contents = contentRepository.searchContents(
            user.getOrganizationId(),
            brandId,
            stage,
            type,
            title,
            pageable
        );

        return contents.map(ContentResponse::fromEntity);
    }

    @Override
    @Transactional
    public ContentResponse updateContent(Long id, ContentRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == com.creatorops.auth.entity.UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Contributors cannot perform this action.");
        }

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot update content outside your organization.");
        }

        Brand newBrand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.brandId()));

        if (!newBrand.getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot transfer content to a brand outside your organization.");
        }

        ContentStage oldStage = content.getStage();
        OffsetDateTime oldPublishDate = content.getPublishDate();

        content.setBrand(newBrand);
        content.setTitle(request.title());
        content.setDescription(request.description());
        content.setType(request.type());
        content.setStage(request.stage());
        content.setPriority(request.priority());
        content.setDueDate(request.dueDate());
        content.setPublishDate(request.publishDate());

        Content updated = contentRepository.save(content);
        org.slf4j.MDC.put("entityId", String.valueOf(updated.getId()));
        log.info("Updated content card: title={}, brandId={}, stage={}", updated.getTitle(), updated.getBrand().getId(), updated.getStage());
        org.slf4j.MDC.remove("entityId");
        domainEventPublisher.publish(new ContentUpdatedEvent(
            user.getId(),
            user.getOrganizationId(),
            updated.getId(),
            updated.getTitle(),
            EventType.CONTENT_UPDATED,
            "Content '" + updated.getTitle() + "' was updated"
        ));

        if (updated.getStage() == ContentStage.SCHEDULED && oldStage != ContentStage.SCHEDULED) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                updated.getId(),
                updated.getTitle(),
                EventType.CONTENT_SCHEDULED,
                "Content '" + updated.getTitle() + "' was scheduled"
            ));
        } else if (updated.getStage() == ContentStage.PUBLISHED && oldStage != ContentStage.PUBLISHED) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                updated.getId(),
                updated.getTitle(),
                EventType.CONTENT_PUBLISHED,
                "Content '" + updated.getTitle() + "' was published"
            ));
        }

        if (oldPublishDate == null && updated.getPublishDate() != null && updated.getStage() != ContentStage.SCHEDULED) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                updated.getId(),
                updated.getTitle(),
                EventType.CONTENT_SCHEDULED,
                "Content '" + updated.getTitle() + "' was scheduled"
            ));
        } else if (oldPublishDate != null && updated.getPublishDate() != null && !oldPublishDate.isEqual(updated.getPublishDate())) {
            domainEventPublisher.publish(new ContentUpdatedEvent(
                user.getId(),
                user.getOrganizationId(),
                updated.getId(),
                updated.getTitle(),
                EventType.CONTENT_RESCHEDULED,
                "Content '" + updated.getTitle() + "' was rescheduled"
            ));
        }

        return ContentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteContent(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == com.creatorops.auth.entity.UserRole.CONTRIBUTOR) {
            throw new AccessDeniedException("Access denied: Contributors cannot perform this action.");
        }

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot delete content outside your organization.");
        }

        domainEventPublisher.publish(new ContentDeletedEvent(
            user.getId(),
            user.getOrganizationId(),
            content.getId(),
            content.getTitle()
        ));

        org.slf4j.MDC.put("entityId", String.valueOf(content.getId()));
        log.info("Deleted content card: title={}, contentId={}", content.getTitle(), content.getId());
        org.slf4j.MDC.remove("entityId");

        contentRepository.delete(content);
    }
}
