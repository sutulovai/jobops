package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateContactRequest(
        @NotBlank String name,
        String title,
        String contactType,
        UUID companyId,
        String linkedInUrl,
        String email,
        String relationshipStrength,
        String source,
        String notes,
        String preferredChannel,
        String status,
        UUID vacancyId,
        UUID applicationId
) {
}
