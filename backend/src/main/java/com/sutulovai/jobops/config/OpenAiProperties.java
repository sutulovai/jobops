package com.sutulovai.jobops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobops.openai")
public record OpenAiProperties(String apiKey, String baseUrl, String model, String analysisModel) {
}
