package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
        UUID id, UUID userId, UUID companyId, String companyName,
        String name, String title, String contactType,
        String linkedInUrl, String email, String relationshipStrength,
        String source, String lastContactedDate, String nextFollowUpDate,
        String notes, String preferredChannel, String status,
        UUID vacancyId, UUID applicationId,
        Instant createdAt, Instant updatedAt
) {}
