package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OutreachMessageResponse(
        UUID id,
        UUID userId,
        UUID contactId,
        String contactName,
        UUID companyId,
        String companyName,
        UUID vacancyId,
        String vacancyTitle,
        UUID applicationId,
        String messageType,
        String channel,
        String recipientType,
        String generatedText,
        String editedFinalText,
        String status,
        String tone,
        int versionNumber,
        UUID nextActionId,
        Instant createdAt,
        Instant copiedAt,
        Instant sentAt
) {
}
