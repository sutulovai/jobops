package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateCompanyRequest(
        @NotBlank String name,
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
        String status
) {
}
