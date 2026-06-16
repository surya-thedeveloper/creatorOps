package com.creatorops.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class mapping to "creatorops.ai" prefix.
 */
@ConfigurationProperties(prefix = "creatorops.ai")
public record AiProperties(
    String provider,
    GeminiProperties gemini
) {
    public AiProperties {
        if (provider == null) {
            provider = "gemini";
        }
        if (gemini == null) {
            gemini = new GeminiProperties(null, "gemini-1.5-flash");
        }
    }

    public record GeminiProperties(
        String apiKey,
        String model
    ) {
        public GeminiProperties {
            if (model == null || model.isBlank()) {
                model = "gemini-1.5-flash";
            }
        }
    }
}
