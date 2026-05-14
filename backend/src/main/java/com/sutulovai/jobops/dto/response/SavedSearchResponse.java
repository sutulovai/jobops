package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SavedSearchResponse(
        UUID id, UUID userId, String title, String platform,
        String url, String queryText, String booleanQuery,
        String city, List<String> keywords, String frequency,
        String lastCheckedAt, String nextCheckDate,
        boolean useful, String yieldRating,
        int jobsAdded, int applicationsCreated, Double responseRate,
        String notes, boolean active, Instant createdAt, Instant updatedAt
) {}
