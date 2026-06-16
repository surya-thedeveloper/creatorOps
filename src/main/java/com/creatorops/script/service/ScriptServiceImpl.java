package com.creatorops.script.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.script.dto.ScriptRequest;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.entity.Script;
import com.creatorops.script.repository.ScriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creatorops.common.event.DomainEventPublisher;
import com.creatorops.common.event.ScriptCreatedEvent;
import com.creatorops.common.event.ScriptUpdatedEvent;
import com.creatorops.common.event.ScriptDeletedEvent;

/**
 * <h3>Why this class exists</h3>
 * {@code ScriptServiceImpl} coordinates transaction states, calculates incremental version counts,
 * and maintains tenant isolation logic for scripts.
 * <p>
 * <h3>Chosen Annotations</h3>
 * <ul>
 *   <li>{@code @Service}: Registers this class as a Spring Service.</li>
 *   <li>{@code @Transactional}: Scopes transaction parameters (with optimization for read-only runs).</li>
 * </ul>
 * <p>
 * <h3>Relationship Design</h3>
 * Integrates with {@link ScriptRepository}, {@link ContentRepository}, and {@link UserRepository}.
 * <p>
 * <h3>How it fits into the creator workflow</h3>
 * Protects scripting content, verifying that all reads, updates, and deletes are strictly restricted to the caller's organization.
 */
@Service
public class ScriptServiceImpl implements ScriptService {

    private final ScriptRepository scriptRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Autowired
    public ScriptServiceImpl(ScriptRepository scriptRepository,
                             ContentRepository contentRepository,
                             UserRepository userRepository,
                             DomainEventPublisher domainEventPublisher) {
        this.scriptRepository = scriptRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public ScriptResponse createScript(Long contentId, String userEmail, ScriptRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        // Calculate next version
        int maxVersion = scriptRepository.findMaxVersionByContentId(contentId);
        int nextVersion = maxVersion + 1;

        Script script = new Script();
        script.setContent(content);
        script.setUser(user);
        script.setVersion(nextVersion);
        script.setDocumentType(request.documentType());
        script.setEditorContent(request.editorContent());
        script.setExternalDocumentUrl(request.externalDocumentUrl());
        script.setUploadedFileReference(request.uploadedFileReference());
        script.setGeneratedScript(request.generatedScript());

        Script saved = scriptRepository.save(script);
        domainEventPublisher.publish(new ScriptCreatedEvent(
            user.getId(),
            user.getOrganizationId(),
            content.getId(),
            saved.getId(),
            saved.getVersion()
        ));
        return ScriptResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScriptResponse getScriptById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Script not found with id: " + id));

        // Enforce tenant boundary
        if (!script.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Script belongs to a different organization.");
        }

        return ScriptResponse.fromEntity(script);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScriptResponse> getScriptsByContent(Long contentId, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        // Enforce tenant boundary
        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Content belongs to a different organization.");
        }

        Page<Script> scripts = scriptRepository.findByContentIdOrderByVersionDesc(contentId, pageable);
        return scripts.map(ScriptResponse::fromEntity);
    }

    @Override
    @Transactional
    public ScriptResponse updateScript(Long id, ScriptRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Script not found with id: " + id));

        // Enforce tenant boundary
        if (!script.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Script belongs to a different organization.");
        }

        script.setDocumentType(request.documentType());
        script.setEditorContent(request.editorContent());
        script.setExternalDocumentUrl(request.externalDocumentUrl());
        script.setUploadedFileReference(request.uploadedFileReference());
        script.setGeneratedScript(request.generatedScript());

        Script updated = scriptRepository.save(script);
        domainEventPublisher.publish(new ScriptUpdatedEvent(
            user.getId(),
            user.getOrganizationId(),
            updated.getContent().getId(),
            updated.getId(),
            updated.getVersion()
        ));
        return ScriptResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteScript(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Script not found with id: " + id));

        // Enforce tenant boundary
        if (!script.getContent().getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Script belongs to a different organization.");
        }

        domainEventPublisher.publish(new ScriptDeletedEvent(
            user.getId(),
            user.getOrganizationId(),
            script.getContent().getId(),
            script.getId(),
            script.getVersion()
        ));

        scriptRepository.delete(script);
    }

    /**
     * <h3>Future AI Extension Hooks</h3>
     * In subsequent phases, these hooks will interface with {@code AiProviderGateway} (e.g., Gemini integration):
     * <ul>
     *   <li><b>AI Script Generation</b>: Gathers all research inputs (notes, links, brainstorm outlines) and invokes Gemini to generate the initial script version (Script Version 1.0).</li>
     *   <li><b>Rewrite Script</b>: Re-prompts the AI with the existing draft and a custom instruction (e.g., "make it more engaging").</li>
     *   <li><b>Improve Hook</b>: Invokes the AI specifically targeting the script's first 30 seconds to generate alternative, high-retention hooks.</li>
     *   <li><b>Expand/Shorten Script</b>: Triggers length adjustments by specifying target read times/word counts to the AI prompt adapter.</li>
     *   <li><b>Make Conversational</b>: Rewrites the script in a more natural, spoken-word style suited for video.</li>
     * </ul>
     */
    public void futureAiScriptExtensionHooksPlaceholder() {
        // Architecture placeholder for future AI script tools
    }
}
