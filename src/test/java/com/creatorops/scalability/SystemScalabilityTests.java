package com.creatorops.scalability;

import com.creatorops.activity.entity.Activity;
import com.creatorops.activity.repository.ActivityRepository;
import com.creatorops.auth.dto.UpdateProfileRequest;
import com.creatorops.auth.dto.UserResponse;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.auth.service.AuthService;
import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.entity.Brand;
import com.creatorops.brand.repository.BrandRepository;
import com.creatorops.brand.service.BrandService;
import com.creatorops.common.event.ContentCreatedEvent;
import com.creatorops.common.event.DomainEventPublisher;
import com.creatorops.content.entity.Content;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.organization.dto.OrganizationRequest;
import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.repository.OrganizationRepository;
import com.creatorops.organization.service.OrganizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SystemScalabilityTests {

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Qualifier("creatorOpsAsyncExecutor")
    private Executor executor;

    private final List<Long> createdActivityIds = new ArrayList<>();
    private final List<Long> createdContentIds = new ArrayList<>();
    private final List<Long> createdBrandIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdOrgIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (Long id : createdActivityIds) {
            try { activityRepository.deleteById(id); } catch (Exception e) {}
        }
        for (Long id : createdContentIds) {
            try { contentRepository.deleteById(id); } catch (Exception e) {}
        }
        for (Long id : createdBrandIds) {
            try { brandRepository.deleteById(id); } catch (Exception e) {}
        }
        for (Long id : createdUserIds) {
            try { userRepository.deleteById(id); } catch (Exception e) {}
        }
        for (Long id : createdOrgIds) {
            try { organizationRepository.deleteById(id); } catch (Exception e) {}
        }

        // Clean caches
        if (cacheManager.getCache("organizations") != null) {
            cacheManager.getCache("organizations").clear();
        }
        if (cacheManager.getCache("brands") != null) {
            cacheManager.getCache("brands").clear();
        }
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }
    }

    @Test
    void testAsyncExecutor_ThreadAndMdcPreservation() throws Exception {
        MDC.put("correlationId", "test-correlation-id-999");
        CompletableFuture<String> correlationIdFuture = new CompletableFuture<>();
        CompletableFuture<String> threadNameFuture = new CompletableFuture<>();

        executor.execute(() -> {
            correlationIdFuture.complete(MDC.get("correlationId"));
            threadNameFuture.complete(Thread.currentThread().getName());
        });

        String resultCorrelationId = correlationIdFuture.get(2, TimeUnit.SECONDS);
        String threadName = threadNameFuture.get(2, TimeUnit.SECONDS);

        assertEquals("test-correlation-id-999", resultCorrelationId);
        assertTrue(threadName.startsWith("creatorops-async-"));
        MDC.clear();
    }

    @Test
    void testDomainEventPublishingAndActivityLogging() throws Exception {
        Organization org = new Organization("Event Org", "http://logo.com");
        org = organizationRepository.save(org);
        createdOrgIds.add(org.getId());

        User user = new User("Performer", "performer@test.com", "hash");
        user.setRole(UserRole.MANAGER);
        user.setOrganization(org);
        user = userRepository.save(user);
        createdUserIds.add(user.getId());

        Brand brand = new Brand("Brand", "desc", "url", org);
        brand = brandRepository.save(brand);
        createdBrandIds.add(brand.getId());

        Content content = new Content(brand, "Event Content", "desc",
                com.creatorops.content.entity.ContentType.YOUTUBE_VIDEO,
                com.creatorops.content.entity.ContentStage.IDEA,
                com.creatorops.content.entity.ContentPriority.MEDIUM,
                null, null);
        content = contentRepository.save(content);
        createdContentIds.add(content.getId());

        final Long userId = user.getId();
        final Long orgId = org.getId();
        final Long contentId = content.getId();
        final String contentTitle = content.getTitle();
        final String contentStageName = content.getStage().name();

        // Publish event within transaction
        transactionTemplate.execute(status -> {
            domainEventPublisher.publish(new ContentCreatedEvent(
                    userId,
                    orgId,
                    contentId,
                    contentTitle,
                    contentStageName
            ));
            return null;
        });

        // Wait for async processing
        Activity loggedActivity = null;
        int maxRetries = 20;
        for (int i = 0; i < maxRetries; i++) {
            List<Activity> activities = activityRepository.findAll();
            for (Activity act : activities) {
                if (act.getContent().getId().equals(contentId) && act.getUser().getId().equals(userId)) {
                    loggedActivity = act;
                    break;
                }
            }
            if (loggedActivity != null) {
                break;
            }
            Thread.sleep(100);
        }

        assertNotNull(loggedActivity, "Activity record should be created asynchronously after commit");
        createdActivityIds.add(loggedActivity.getId());
        assertEquals("Content '" + contentTitle + "' was created", loggedActivity.getDescription());
        assertEquals(com.creatorops.activity.entity.EventType.CONTENT_CREATED, loggedActivity.getEventType());
    }

    @Test
    void testOrganizationCachingAndEviction() {
        Organization org = new Organization("Cache Test Org", "http://cache-logo.com");
        org = organizationRepository.save(org);
        createdOrgIds.add(org.getId());

        Long orgId = org.getId();

        // 1. Initial findById call - should hit DB and populate cache
        Optional<Organization> orgOpt1 = organizationRepository.findById(orgId);
        assertTrue(orgOpt1.isPresent());

        // Verify cache entry exists
        org.springframework.cache.Cache cache = cacheManager.getCache("organizations");
        assertNotNull(cache);
        org.springframework.cache.Cache.ValueWrapper val = cache.get(orgId);
        assertNotNull(val);
        Organization cachedOrg = (Organization) val.get();
        assertEquals("Cache Test Org", cachedOrg.getName());

        // 2. Call updateOrganization - should evict organization cache entry
        OrganizationRequest updateReq = new OrganizationRequest("Updated Cache Test Org", "http://new-logo.com");
        User admin = new User("Org Admin", "org_admin_cache@test.com", "hash");
        admin.setRole(UserRole.ADMIN);
        admin.setOrganization(org);
        admin = userRepository.save(admin);
        createdUserIds.add(admin.getId());

        organizationService.updateOrganization(orgId, updateReq, admin.getEmail());

        // Verify cache entry is evicted (null)
        val = cache.get(orgId);
        assertNull(val, "Organization cache should be evicted after update");
    }

    @Test
    void testBrandCachingAndEviction() {
        Organization org = new Organization("Brand Cache Org", "http://brand-cache.com");
        org = organizationRepository.save(org);
        createdOrgIds.add(org.getId());

        User admin = new User("Brand Admin", "brand_admin_cache@test.com", "hash");
        admin.setRole(UserRole.ADMIN);
        admin.setOrganization(org);
        admin = userRepository.save(admin);
        createdUserIds.add(admin.getId());

        Brand brand = new Brand("Cache Test Brand", "desc", "url", org);
        brand = brandRepository.save(brand);
        createdBrandIds.add(brand.getId());

        Long brandId = brand.getId();

        // 1. findById call - should populate brands cache
        Optional<Brand> brandOpt1 = brandRepository.findById(brandId);
        assertTrue(brandOpt1.isPresent());

        org.springframework.cache.Cache cache = cacheManager.getCache("brands");
        assertNotNull(cache);
        org.springframework.cache.Cache.ValueWrapper val = cache.get(brandId);
        assertNotNull(val);

        // 2. getBrands call - should also populate cache
        brandService.getBrands(admin.getEmail(), PageRequest.of(0, 10));

        // 3. createBrand call - should evict all entries in brands cache
        BrandRequest createReq = new BrandRequest("New Brand", "desc", "url");
        brandService.createBrand(admin.getEmail(), createReq);

        // Verify findById cache key is evicted
        val = cache.get(brandId);
        assertNull(val, "Brand cache should be evicted after new brand creation");
    }

    @Test
    void testUserCachingAndEviction() {
        Organization org = new Organization("User Cache Org", "http://user-cache.com");
        org = organizationRepository.save(org);
        createdOrgIds.add(org.getId());

        User user = new User("User Cache Test", "user_cache@test.com", "hash");
        user.setRole(UserRole.ADMIN);
        user.setOrganization(org);
        user = userRepository.save(user);
        createdUserIds.add(user.getId());

        String email = user.getEmail();

        // 1. getCurrentUser - should populate cache
        UserResponse response1 = authService.getCurrentUser(email);
        assertNotNull(response1);

        org.springframework.cache.Cache cache = cacheManager.getCache("users");
        assertNotNull(cache);
        org.springframework.cache.Cache.ValueWrapper val = cache.get("dto-" + email);
        assertNotNull(val);

        // 2. updateProfile - should evict cache
        UpdateProfileRequest profileReq = new UpdateProfileRequest("Updated Name", "http://new-image.com");
        authService.updateProfile(email, profileReq);

        // Verify cache entry is evicted
        val = cache.get("dto-" + email);
        assertNull(val, "User cache should be evicted after updateProfile");
    }
}
