package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        UUID userId,
        String name,
        String website,
        String careersPageUrl,
        String linkedInUrl,
        String city,
        String country,
        List<String> officeLocations,
        String remotePolicy,
        String industry,
        String companySize,
        String fundingStatus,
        String priorityTier,
        String englishLikelihood,
        String relocationFriendly,
        String visaSponsorship,
        Integer salaryPitchMin,
        Integer salaryPitchMax,
        String companyType,
        String fitReason,
        List<String> likelyRoles,
        String recommendedStrategy,
        String notes,
        String sourceUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
