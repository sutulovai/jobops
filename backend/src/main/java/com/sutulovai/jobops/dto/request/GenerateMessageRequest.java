package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record GenerateMessageRequest(
        @NotBlank String messageType,
        UUID contactId,
        UUID companyId,
        UUID vacancyId,
        UUID applicationId,
        String channel,
        String tone,
        String lengthTarget,
        String customInstructions
) {
}
