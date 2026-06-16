package com.creatorops.scalability;

import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * A TestExecutionListener that automatically clears all Spring caches before each test method executes.
 * This prevents cross-test cache pollution or stale states from rolled-back database transactions.
 */
public class CacheClearingTestListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        try {
            CacheManager cacheManager = testContext.getApplicationContext().getBean(CacheManager.class);
            for (String cacheName : cacheManager.getCacheNames()) {
                if (cacheManager.getCache(cacheName) != null) {
                    cacheManager.getCache(cacheName).clear();
                }
            }
        } catch (Exception e) {
            // Ignore if CacheManager is not yet initialized or available in the test context
        }
    }
}
