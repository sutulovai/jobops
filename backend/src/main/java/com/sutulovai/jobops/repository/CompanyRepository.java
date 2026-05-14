package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    List<CompanyRow> findByUserId(UUID userId);
    Optional<CompanyRow> findById(UUID id, UUID userId);
    Optional<CompanyRow> findByName(String name, UUID userId);
    CompanyRow save(CompanyRow company);
    void delete(UUID id, UUID userId);

    record CompanyRow(
            UUID id, UUID userId,
            String name, String website, String careersPageUrl, String linkedInUrl,
            String city, String country, String[] officeLocations, String remotePolicy,
            String industry, String companySize, String fundingStatus,
            String priorityTier, String englishLikelihood, String relocationFriendly,
            String visaSponsorship, Integer salaryPitchMin, Integer salaryPitchMax,
            String companyType, String fitReason, String[] likelyRoles,
            String recommendedStrategy, String notes, String sourceUrl,
            String status, Instant createdAt, Instant updatedAt
    ) {}
}
