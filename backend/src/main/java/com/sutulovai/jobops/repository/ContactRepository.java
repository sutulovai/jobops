package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository {
    List<ContactRow> findByUserId(UUID userId);
    Optional<ContactRow> findById(UUID id, UUID userId);
    List<ContactRow> findByCompanyId(UUID companyId, UUID userId);
    ContactRow save(ContactRow contact);
    void delete(UUID id, UUID userId);

    record ContactRow(
            UUID id, UUID userId, UUID companyId,
            String name, String title, String contactType,
            String linkedInUrl, String email, String relationshipStrength,
            String source, String lastContactedDate, String nextFollowUpDate,
            String notes, String preferredChannel, String status,
            UUID vacancyId, UUID applicationId,
            Instant createdAt, Instant updatedAt
    ) {}
}
