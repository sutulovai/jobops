package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedSearchRepository {
    List<SearchRow> findByUserId(UUID userId);
    Optional<SearchRow> findById(UUID id, UUID userId);
    SearchRow save(SearchRow search);
    void markChecked(UUID id, UUID userId, String nextCheckDate);
    void delete(UUID id, UUID userId);

    record SearchRow(
            UUID id, UUID userId, String title, String platform,
            String url, String queryText, String booleanQuery,
            String city, String[] keywords, String frequency,
            String lastCheckedDate, String nextCheckDate,
            boolean useful, String yieldRating,
            int jobsAddedCount, int applicationsCreatedCount, int responsesFromCount,
            String notes, boolean active, Instant createdAt, Instant updatedAt
    ) {}
}
