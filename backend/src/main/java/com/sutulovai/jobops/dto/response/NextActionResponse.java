package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NextActionResponse(
        UUID id,
        UUID userId,
        String actionType,
        String priority,
        int priorityScore,
        String dueDate,
        String status,
        String reason,
        UUID companyId,
        String companyName,
        UUID vacancyId,
        String vacancyTitle,
        UUID applicationId,
        UUID contactId,
        String contactName,
        UUID messageId,
        UUID savedSearchId,
        boolean generatedMessageRequired,
        String recommendedMessageType,
        String snoozedUntil,
        String skippedUntil,
        Instant createdAt,
        Instant completedAt
) {
}
