package com.creatorops.reliability;

import com.creatorops.ai.exception.AiGenerationException;
import com.creatorops.ai.provider.AIProvider;
import com.creatorops.ai.service.AIServiceImpl;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.event.DomainEventPublisher;
import com.creatorops.common.exception.FeatureDisabledException;
import com.creatorops.common.feature.FeatureFlagService;
import com.creatorops.common.metrics.MetricsService;
import com.creatorops.config.FeatureProperties;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.research.repository.ResearchItemRepository;
import com.creatorops.script.service.ScriptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FeatureFlagTests {

    private FeatureProperties properties;
    private FeatureFlagService featureFlagService;
    private AIServiceImpl aiService;

    @BeforeEach
    public void setUp() {
        properties = mock(FeatureProperties.class);
        featureFlagService = new FeatureFlagService(properties);

        UserRepository userRepository = mock(UserRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        ResearchItemRepository researchItemRepository = mock(ResearchItemRepository.class);
        ScriptService scriptService = mock(ScriptService.class);
        DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);
        AIProvider aiProvider = mock(AIProvider.class);
        MetricsService metricsService = mock(MetricsService.class);

        aiService = new AIServiceImpl(
                userRepository,
                contentRepository,
                researchItemRepository,
                scriptService,
                domainEventPublisher,
                aiProvider,
                metricsService,
                featureFlagService
        );
    }

    @Test
    public void testFeatureDisabledThrowsException() {
        // Mock all feature flags as disabled
        when(properties.aiEnabled()).thenReturn(false);
        when(properties.aiBrainstormEnabled()).thenReturn(false);
        when(properties.aiScriptEnabled()).thenReturn(false);

        assertFalse(featureFlagService.isAiEnabled());
        assertFalse(featureFlagService.isAiBrainstormEnabled());
        assertFalse(featureFlagService.isAiScriptEnabled());

        assertThrows(FeatureDisabledException.class, () -> aiService.generateBrainstorm(1L, "user@example.com"));
        assertThrows(FeatureDisabledException.class, () -> aiService.generateScript(1L, "user@example.com"));
    }

    @Test
    public void testFeatureEnabledAllowsOperation() {
        when(properties.aiEnabled()).thenReturn(true);
        when(properties.aiBrainstormEnabled()).thenReturn(true);
        when(properties.aiScriptEnabled()).thenReturn(true);

        assertTrue(featureFlagService.isAiEnabled());
        assertTrue(featureFlagService.isAiBrainstormEnabled());
        assertTrue(featureFlagService.isAiScriptEnabled());
        
        // It passes feature flag checks and proceeds to tenant/user validation (which throws ResourceNotFoundException because userRepository is mocked)
        // This validates that the FeatureDisabledException is NOT thrown!
        assertThrows(RuntimeException.class, () -> {
            try {
                aiService.generateBrainstorm(1L, "user@example.com");
            } catch (FeatureDisabledException e) {
                fail("Should not throw FeatureDisabledException");
            }
        });
    }
}
