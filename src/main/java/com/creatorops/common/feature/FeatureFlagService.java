package com.creatorops.common.feature;

import com.creatorops.config.FeatureProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

    private final FeatureProperties properties;

    @Autowired
    public FeatureFlagService(FeatureProperties properties) {
        this.properties = properties;
    }

    public boolean isAiEnabled() {
        return properties.aiEnabled();
    }

    public boolean isAiBrainstormEnabled() {
        return properties.aiEnabled() && properties.aiBrainstormEnabled();
    }

    public boolean isAiScriptEnabled() {
        return properties.aiEnabled() && properties.aiScriptEnabled();
    }

    public boolean isAnalyticsEnabled() {
        return properties.analyticsEnabled();
    }
}
