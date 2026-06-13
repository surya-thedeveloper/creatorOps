package com.creatorops.research.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.research.dto.ResearchItemRequest;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItem;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.research.repository.ResearchItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h3>Why this class exists</h3>
 * {@code ResearchItemServiceImpl} executes business rules, checks tenant context boundaries,
 * and tracks research item lifecycle operations.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Service}: Registers this class as a Spring Service bean.</li>
 *   <li>{@code @Transactional}: Wraps public write operations in transactions and applies read-only optimizations.</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * Coordinates between {@link UserRepository}, {@link ContentRepository}, and {@link ResearchItemRepository}.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Secures research card entries, verifying that all edits and reads are strictly restricted to the user's organization scope.
 * <p>
 * <h3>Future AI Support Placeholders</h3>
 * This service is pre-structured to integrate AI-driven outline/brainstorm operations.
 * In subsequent phases, trigger calls will route Content context to the custom AI Provider Gateway
 * (e.g. {@code GeminiProviderAdapter}) and store returned text segments directly as {@code ResearchItemType.AI_BRAINSTORM} records.
 */
@Service
public class ResearchItemServiceImpl implements ResearchItemService {

    private final ResearchItemRepository researchItemRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ResearchItemServiceImpl(ResearchItemRepository researchItemRepository,
                                  ContentRepository contentRepository,
                                  UserRepository userRepository) {
        this.researchItemRepository = researchItemRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ResearchItemResponse createResearchItem(Long contentId, String userEmail, ResearchItemRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary: content's brand organization must match user's organization
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot add research to content outside your organization.");
        }

        ResearchItem item = new ResearchItem(
            content,
            user,
            request.type(),
            request.title(),
            request.content(),
            request.externalUrl()
        );

        ResearchItem saved = researchItemRepository.save(item);
        return ResearchItemResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResearchItemResponse getResearchItemById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ResearchItem item = researchItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research item not found with id: " + id));

        // Enforce tenant boundary: research item's parent content brand organization must match user's organization
        if (!item.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot view research outside your organization.");
        }

        return ResearchItemResponse.fromEntity(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResearchItemResponse> getResearchItemsByContent(Long contentId, ResearchItemType type, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot list research under content outside your organization.");
        }

        Page<ResearchItem> items;
        if (type != null) {
            items = researchItemRepository.findByContentIdAndType(contentId, type, pageable);
        } else {
            items = researchItemRepository.findByContentId(contentId, pageable);
        }

        return items.map(ResearchItemResponse::fromEntity);
    }

    @Override
    @Transactional
    public ResearchItemResponse updateResearchItem(Long id, ResearchItemRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ResearchItem item = researchItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research item not found with id: " + id));

        // Enforce tenant boundary
        if (!item.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot update research outside your organization.");
        }

        item.setType(request.type());
        item.setTitle(request.title());
        item.setContentText(request.content());
        item.setUrl(request.externalUrl());

        ResearchItem updated = researchItemRepository.save(item);
        return ResearchItemResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteResearchItem(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ResearchItem item = researchItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Research item not found with id: " + id));

        // Enforce tenant boundary
        if (!item.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot delete research outside your organization.");
        }

        researchItemRepository.delete(item);
    }

    /**
     * <h3>Future AI Extension Endpoint Hooks</h3>
     * This placeholder details how downstream integration tasks will connect:
     * <pre>{@code
     * public ResearchItemResponse triggerAIBrainstorm(Long contentId, String userEmail, String promptTopic) {
     *     // 1. Fetch content card and verify tenant boundaries.
     *     // 2. Format a system context combining brand guidelines and target content topic.
     *     // 3. Invoke concrete adapter implementation of the abstract gateway:
     *     //    String aiGeneratedText = aiProviderGateway.generateBrainstormResponse(promptTopic);
     *     // 4. Save and return output:
     *     //    ResearchItem item = new ResearchItem(content, user, ResearchItemType.AI_BRAINSTORM, "AI Angles", aiGeneratedText, null);
     *     //    return ResearchItemResponse.fromEntity(researchItemRepository.save(item));
     * }
     * }</pre>
     */
    private void futureAiBrainstormIntegrationPlaceholder() {
        // Placeholder outline logic documenting downstream hooks
    }
}
