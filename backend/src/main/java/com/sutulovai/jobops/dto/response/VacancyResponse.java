package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VacancyResponse(
        UUID id,
        UUID userId,
        UUID companyId,
        String companyName,
        String title,
        String location,
        String remotePolicy,
        String url,
        String sourceChannel,
        List<String> stackKeywords,
        List<String> domainKeywords,
        Integer salaryRangeMin,
        Integer salaryRangeMax,
        String salaryCurrency,
        String languageRequirement,
        String relocationVisaWording,
        String seniority,
        String employmentType,
        String status,
        Integer aiFitScore,
        Integer aiConfidence,
        String aiRecommendation,
        String aiReasoning,
        List<String> redFlags,
        List<String> uncertaintyFlags,
        String discoveredDate,
        Instant createdAt,
        Instant updatedAt
) {
}
