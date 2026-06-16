package com.creatorops.ai.service;

import com.creatorops.ai.exception.AiGenerationException;
import com.creatorops.ai.provider.AIProvider;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItem;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.research.repository.ResearchItemRepository;
import com.creatorops.script.dto.ScriptRequest;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.entity.DocumentType;
import com.creatorops.script.service.ScriptService;
import com.creatorops.common.event.DomainEventPublisher;
import com.creatorops.common.event.AiBrainstormGeneratedEvent;
import com.creatorops.common.event.AiScriptGeneratedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <h3>AIServiceImpl</h3>
 * Coordinates prompt engineering, AI provider execution, and module saves, enforcing tenant boundary checks.
 * <p>
 * <h3>Why provider abstraction was chosen / Why AIProvider exists</h3>
 * The {@link AIProvider} interface isolates the business logic from vendor-specific libraries or models.
 * Standardizing on a contract allows changing the backend generator (e.g. from Google Gemini to OpenAI or a local Ollama)
 * at any time via simple configuration settings, without code modifications.
 * <p>
 * <h3>Why Gemini is isolated behind an interface</h3>
 * Upstream APIs frequently update their endpoint URL structures, JSON formats, or header settings. Isolating
 * the Google Gemini REST call behind the {@link AIProvider} contract restricts vendor changes to the adapter layer,
 * keeping the core domain clean.
 * <p>
 * <h3>How prompt construction works</h3>
 * Prompts are formulated dynamically in {@link PromptBuilder} by combining content descriptors (title, type, description)
 * with the compiled user research (notes, link references, and past brainstorm outputs). This provides rich context,
 * ensuring high-quality, relevant model outputs.
 * <p>
 * <h3>How AI integrates with existing modules</h3>
 * The AI module acts as an automation layer over the existing domain modules:
 * <ul>
 *   <li>The brainstorm result is saved directly as a {@link ResearchItem} of type {@code AI_BRAINSTORM}.</li>
 *   <li>The script draft is saved using the existing {@link ScriptService}, creating an incremental internal script version.</li>
 * </ul>
 * This reuse of existing data structures and service methods prevents schema duplication and ensures that standard
 * database validations, index constraints, and cascading hard deletes continue to function.
 */
@Service
public class AIServiceImpl implements AIService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AIServiceImpl.class);

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ResearchItemRepository researchItemRepository;
    private final ScriptService scriptService;
    private final DomainEventPublisher domainEventPublisher;
    private final AIProvider aiProvider;

    @Autowired
    public AIServiceImpl(UserRepository userRepository,
                         ContentRepository contentRepository,
                         ResearchItemRepository researchItemRepository,
                         ScriptService scriptService,
                         DomainEventPublisher domainEventPublisher,
                         AIProvider aiProvider) {
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.researchItemRepository = researchItemRepository;
        this.scriptService = scriptService;
        this.domainEventPublisher = domainEventPublisher;
        this.aiProvider = aiProvider;
    }

    private User validateTenant(Long contentId, String userEmail, Content[] contentOut) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        if (!content.getBrand().getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to access content in this organization.");
        }

        contentOut[0] = content;
        return user;
    }

    @Override
    @Transactional
    public ResearchItemResponse generateBrainstorm(Long contentId, String userEmail) {
        Content[] contentWrapper = new Content[1];
        User user = validateTenant(contentId, userEmail, contentWrapper);
        Content content = contentWrapper[0];

        // Fetch all research items for prompt compilation
        List<ResearchItem> allResearch = researchItemRepository.findByContentId(contentId, PageRequest.of(0, 1000)).getContent();
        List<ResearchItem> notes = allResearch.stream().filter(r -> r.getType() == ResearchItemType.NOTE).toList();
        List<ResearchItem> links = allResearch.stream().filter(r -> r.getType() == ResearchItemType.LINK).toList();
        List<ResearchItem> existingBrainstorms = allResearch.stream().filter(r -> r.getType() == ResearchItemType.AI_BRAINSTORM).toList();

        // 1. Build prompt
        String prompt = PromptBuilder.buildBrainstormPrompt(content, notes, links, existingBrainstorms);

        // 2. Invoke provider abstraction
        String generatedResult = aiProvider.generateBrainstorm(prompt);
        if (generatedResult == null || generatedResult.isBlank()) {
            throw new AiGenerationException("AI provider generated an empty brainstorm outline.");
        }

        // 3. Save as ResearchItem of type AI_BRAINSTORM
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        ResearchItem brainstormItem = new ResearchItem(
                content,
                user,
                ResearchItemType.AI_BRAINSTORM,
                "AI Brainstorm - " + timestamp,
                generatedResult,
                null
        );

        ResearchItem saved = researchItemRepository.save(brainstormItem);
        org.slf4j.MDC.put("entityId", String.valueOf(saved.getId()));
        log.info("AI brainstorm generated: title={}, contentId={}", saved.getTitle(), contentId);
        org.slf4j.MDC.remove("entityId");

        // 4. Publish Domain Event
        domainEventPublisher.publish(new AiBrainstormGeneratedEvent(
                user.getId(),
                user.getOrganizationId(),
                content.getId(),
                saved.getId(),
                saved.getTitle()
        ));

        return ResearchItemResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ScriptResponse generateScript(Long contentId, String userEmail) {
        Content[] contentWrapper = new Content[1];
        User user = validateTenant(contentId, userEmail, contentWrapper);
        Content content = contentWrapper[0];

        // Fetch all research items for prompt compilation
        List<ResearchItem> allResearch = researchItemRepository.findByContentId(contentId, PageRequest.of(0, 1000)).getContent();
        List<ResearchItem> notes = allResearch.stream().filter(r -> r.getType() == ResearchItemType.NOTE).toList();
        List<ResearchItem> links = allResearch.stream().filter(r -> r.getType() == ResearchItemType.LINK).toList();
        List<ResearchItem> brainstorms = allResearch.stream().filter(r -> r.getType() == ResearchItemType.AI_BRAINSTORM).toList();

        // 1. Build prompt
        String prompt = PromptBuilder.buildScriptPrompt(content, notes, links, brainstorms);

        // 2. Invoke provider abstraction
        String generatedScript = aiProvider.generateScript(prompt);
        if (generatedScript == null || generatedScript.isBlank()) {
            throw new AiGenerationException("AI provider generated an empty script draft.");
        }

        // 3. Create script version via ScriptService
        ScriptRequest scriptRequest = new ScriptRequest(
                DocumentType.INTERNAL,
                generatedScript,
                null,
                null,
                generatedScript
        );

        ScriptResponse scriptResponse = scriptService.createScript(contentId, userEmail, scriptRequest);
        org.slf4j.MDC.put("entityId", String.valueOf(scriptResponse.id()));
        log.info("AI script draft generated: version={}, contentId={}", scriptResponse.version(), contentId);
        org.slf4j.MDC.remove("entityId");

        // 4. Publish Domain Event
        domainEventPublisher.publish(new AiScriptGeneratedEvent(
                user.getId(),
                user.getOrganizationId(),
                content.getId(),
                scriptResponse.id(),
                scriptResponse.version()
        ));

        return scriptResponse;
    }
}
