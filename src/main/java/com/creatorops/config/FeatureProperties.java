package com.creatorops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "creatorops.features")
public record FeatureProperties(
    boolean aiEnabled,
    boolean aiBrainstormEnabled,
    boolean aiScriptEnabled,
    boolean analyticsEnabled
) {
    public FeatureProperties {
        // Safe defaults if properties are omitted
    }
}
