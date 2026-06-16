package com.creatorops.calendar;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.security.JwtService;
import com.creatorops.calendar.dto.CalendarItemResponse;
import com.creatorops.calendar.service.CalendarService;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import com.creatorops.organization.entity.Organization;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User contributorUser;
    private Organization organization;
    private CalendarItemResponse validEvent;

    @BeforeEach
    void setUp() {
        organization = new Organization("Slay Media", null);
        organization.setId(1L);

        contributorUser = new User("Bruce Contributor", "bruce@slay.com", "hashed_password");
        contributorUser.setId(3L);
        contributorUser.setRole(UserRole.CONTRIBUTOR);
        contributorUser.setOrganization(organization);

        validEvent = new CalendarItemResponse(
            100L,
            "Summer Fashion Guide",
            5L,
            "SLAY Fashion",
            ContentType.YOUTUBE_VIDEO,
            ContentStage.SCHEDULED,
            OffsetDateTime.now().plusDays(2),
            OffsetDateTime.now().plusDays(5),
            ContentPriority.HIGH
        );
    }

    private void mockAuth(User user) {
        when(jwtService.extractUsername(anyString())).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void getCalendarRange_Success() throws Exception {
        mockAuth(contributorUser);
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(10);

        when(calendarService.getCalendarRange(
                eq("bruce@slay.com"), any(OffsetDateTime.class), any(OffsetDateTime.class), eq(5L), eq(ContentType.YOUTUBE_VIDEO), eq(ContentStage.SCHEDULED)))
                .thenReturn(List.of(validEvent));

        mockMvc.perform(get("/api/calendar?startDate=" + start + "&endDate=" + end + "&brandId=5&contentType=YOUTUBE_VIDEO&stage=SCHEDULED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentId").value(100))
                .andExpect(jsonPath("$[0].title").value("Summer Fashion Guide"));
    }

    @Test
    void getCalendarRange_ValidationFailed_StartDateAfterEndDate() throws Exception {
        mockAuth(contributorUser);
        OffsetDateTime start = OffsetDateTime.now().plusDays(10);
        OffsetDateTime end = OffsetDateTime.now();

        when(calendarService.getCalendarRange(
                eq("bruce@slay.com"), any(OffsetDateTime.class), any(OffsetDateTime.class), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Validation failed: Start date must be before or equal to end date."));

        mockMvc.perform(get("/api/calendar?startDate=" + start + "&endDate=" + end)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed: Start date must be before or equal to end date."));
    }

    @Test
    void getCalendarRange_ValidationFailed_MalformedEnum() throws Exception {
        mockAuth(contributorUser);
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = OffsetDateTime.now().plusDays(10);

        mockMvc.perform(get("/api/calendar?startDate=" + start + "&endDate=" + end + "&stage=INVALID_STAGE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'stage': INVALID_STAGE"));
    }

    @Test
    void getUpcomingContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(calendarService.getUpcomingContent(eq("bruce@slay.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validEvent), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/calendar/upcoming")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentId").value(100))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    void getScheduledContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(calendarService.getScheduledContent(eq("bruce@slay.com"), eq(5L), eq(ContentType.YOUTUBE_VIDEO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validEvent), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/calendar/scheduled?brandId=5&contentType=YOUTUBE_VIDEO")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentId").value(100));
    }

    @Test
    void getPublishedContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(calendarService.getPublishedContent(eq("bruce@slay.com"), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validEvent), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/calendar/published")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentId").value(100));
    }

    @Test
    void getOverdueContent_Success() throws Exception {
        mockAuth(contributorUser);
        when(calendarService.getOverdueContent(eq("bruce@slay.com"))).thenReturn(List.of(validEvent));

        mockMvc.perform(get("/api/calendar/overdue")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentId").value(100));
    }
}
