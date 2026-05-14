package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record LogCopiedMessageRequest(
        @NotBlank String messageType,
        @NotBlank String bodyText,
        UUID contactId,
        UUID companyId,
        UUID vacancyId,
        UUID applicationId,
        String channel
) {
}
