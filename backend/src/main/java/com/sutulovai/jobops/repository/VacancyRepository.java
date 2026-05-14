package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacancyRepository {
    List<VacancyRow> findByUserId(UUID userId, String status);
    Optional<VacancyRow> findById(UUID id, UUID userId);
    VacancyRow save(VacancyRow vacancy);
    void updateStatus(UUID id, UUID userId, String status);
    void delete(UUID id, UUID userId);

    record VacancyRow(
            UUID id, UUID userId, UUID companyId,
            String title, String location, String remotePolicy,
            String url, String sourceChannel, String jobDescriptionText,
            String[] stackKeywords, String[] domainKeywords,
            Integer salaryRangeMin, Integer salaryRangeMax, String salaryCurrency,
            String languageRequirement, String relocationVisaWording,
            String seniority, String employmentType, String status,
            Integer aiFitScore, Integer aiConfidence, String aiRecommendation, String aiReasoning,
            String[] redFlags, String[] uncertaintyFlags,
            LocalDate discoveredDate, Instant createdAt, Instant updatedAt
    ) {}
}
