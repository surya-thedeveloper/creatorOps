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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ContentRepository contentRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    @Autowired
    public ContentServiceImpl(ContentRepository contentRepository,
                              BrandRepository brandRepository,
                              UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.brandRepository = brandRepository;
        this.userRepository = userRepository;
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

        content.setBrand(newBrand);
        content.setTitle(request.title());
        content.setDescription(request.description());
        content.setType(request.type());
        content.setStage(request.stage());
        content.setPriority(request.priority());
        content.setDueDate(request.dueDate());
        content.setPublishDate(request.publishDate());

        Content updated = contentRepository.save(content);
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

        contentRepository.delete(content);
    }
}
