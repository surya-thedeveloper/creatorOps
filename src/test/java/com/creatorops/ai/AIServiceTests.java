package com.creatorops.ai;

import com.creatorops.ai.provider.AIProvider;
import com.creatorops.ai.service.AIServiceImpl;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.brand.entity.Brand;
import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.organization.entity.Organization;
import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.research.entity.ResearchItem;
import com.creatorops.research.entity.ResearchItemType;
import com.creatorops.research.repository.ResearchItemRepository;
import com.creatorops.script.dto.ScriptResponse;
import com.creatorops.script.entity.DocumentType;
import com.creatorops.script.service.ScriptService;
import com.creatorops.activity.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ResearchItemRepository researchItemRepository;

    @Mock
    private ScriptService scriptService;

    @Mock
    private ActivityService activityService;

    @Mock
    private AIProvider aiProvider;

    @InjectMocks
    private AIServiceImpl aiService;

    private User user;
    private Organization organization;
    private Brand brand;
    private Content content;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        brand = new Brand("SLAY Tech", "Tech review brand", null, organization);
        brand.setId(5L);

        user = new User("Tony Admin", "tony@slay.com", "hashed_password");
        user.setId(2L);
        user.setRole(UserRole.ADMIN);
        user.setOrganization(organization);

        content = new Content(brand, "How to write a compiler in Java", "A programming tutorial", ContentType.BLOG, ContentStage.RESEARCH, ContentPriority.HIGH, null, null);
        content.setId(10L);
    }

    @Test
    void generateBrainstorm_Success() {
        when(userRepository.findByEmail("tony@slay.com")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));

        ResearchItem note = new ResearchItem(content, user, ResearchItemType.NOTE, "My Note", "Java compiler tutorial notes", null);
        when(researchItemRepository.findByContentId(eq(10L), any())).thenReturn(new PageImpl<>(List.of(note)));

        when(aiProvider.generateBrainstorm(anyString())).thenReturn("AI brain ideas output");
        when(researchItemRepository.save(any(ResearchItem.class))).thenAnswer(invocation -> {
            ResearchItem item = invocation.getArgument(0);
            item.setId(20L);
            return item;
        });

        ResearchItemResponse response = aiService.generateBrainstorm(10L, "tony@slay.com");

        assertNotNull(response);
        assertEquals(20L, response.id());
        String expectedTitle = "AI Brainstorm - " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        assertEquals(expectedTitle, response.title());
        assertEquals("AI brain ideas output", response.content());
        verify(activityService, times(1)).record(any(), any(), any(), any(), eq(20L), anyString(), any());
    }

    @Test
    void generateScript_Success() {
        when(userRepository.findByEmail("tony@slay.com")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(content));

        when(researchItemRepository.findByContentId(eq(10L), any())).thenReturn(new PageImpl<>(List.of()));

        when(aiProvider.generateScript(anyString())).thenReturn("Conversational script text");

        ScriptResponse mockScriptResponse = new ScriptResponse(
                30L,
                10L,
                1,
                "Conversational script text",
                "Conversational script text",
                DocumentType.INTERNAL,
                null,
                null,
                2L,
                "Tony Admin",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(scriptService.createScript(eq(10L), eq("tony@slay.com"), any())).thenReturn(mockScriptResponse);

        ScriptResponse response = aiService.generateScript(10L, "tony@slay.com");

        assertNotNull(response);
        assertEquals(30L, response.id());
        assertEquals(1, response.version());
        verify(activityService, times(1)).record(any(), any(), any(), any(), eq(30L), anyString(), any());
    }

    @Test
    void tenantBoundaryCheck_Mismatch_ThrowsException() {
        Organization otherOrg = new Organization("Other Org", null);
        otherOrg.setId(99L);
        Brand otherBrand = new Brand("Other Brand", "description", null, otherOrg);
        Content otherContent = new Content(otherBrand, "title", "desc", ContentType.BLOG, ContentStage.IDEA, ContentPriority.LOW, null, null);
        otherContent.setId(10L);

        when(userRepository.findByEmail("tony@slay.com")).thenReturn(Optional.of(user));
        when(contentRepository.findById(10L)).thenReturn(Optional.of(otherContent));

        assertThrows(AccessDeniedException.class, () -> aiService.generateBrainstorm(10L, "tony@slay.com"));
    }
}
