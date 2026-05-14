package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {
    List<ApplicationRow> findByUserId(UUID userId);
    Optional<ApplicationRow> findById(UUID id, UUID userId);
    Optional<ApplicationRow> findByVacancyId(UUID vacancyId, UUID userId);
    ApplicationRow save(ApplicationRow app);
    void updateStage(UUID id, UUID userId, String stage);
    void incrementFollowUp(UUID id, UUID userId);
    void markStale(UUID id, UUID userId);
    List<ApplicationRow> findStaleApplications(UUID userId, int daysThreshold);
    void setRecruiterContacted(UUID id, UUID userId);
    void setHiringManagerContacted(UUID id, UUID userId);
    void setReferralRequested(UUID id, UUID userId);
    void updateLastContactDate(UUID id, UUID userId);

    record ApplicationRow(
            UUID id, UUID userId, UUID vacancyId, UUID companyId, UUID cvId,
            String stage, String applicationChannel, String sourceChannel,
            LocalDate dateApplied, boolean recruiterContacted,
            boolean hiringManagerContacted, boolean referralRequested,
            int followUpCount, LocalDate lastContactDate, LocalDate nextActionDate,
            int priority, boolean stale, String notes, String outcome,
            String rejectionReason, String cityCategory,
            Instant createdAt, Instant updatedAt
    ) {}
}
