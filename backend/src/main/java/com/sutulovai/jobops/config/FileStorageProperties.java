package com.sutulovai.jobops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobops.file-storage")
public record FileStorageProperties(String root) {
}
