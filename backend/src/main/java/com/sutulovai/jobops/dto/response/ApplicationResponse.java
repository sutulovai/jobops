package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID userId,
        UUID vacancyId,
        String vacancyTitle,
        UUID companyId,
        String companyName,
        UUID cvId,
        String stage,
        String applicationChannel,
        String sourceChannel,
        String dateApplied,
        boolean recruiterContacted,
        boolean hiringManagerContacted,
        boolean referralRequested,
        int followUpCount,
        String lastContactDate,
        String nextActionDate,
        int priority,
        boolean stale,
        String notes,
        String outcome,
        String rejectionReason,
        String cityCategory,
        Instant createdAt,
        Instant updatedAt
) {
}
