package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutreachMessageRepository {
    List<MessageRow> findByUserId(UUID userId);
    Optional<MessageRow> findById(UUID id, UUID userId);
    MessageRow save(MessageRow message);
    void markCopied(UUID id, UUID userId);
    void markSent(UUID id, UUID userId);

    record MessageRow(
            UUID id, UUID userId,
            UUID contactId, UUID companyId, UUID vacancyId, UUID applicationId,
            String messageType, String channel, String recipientType,
            String generatedText, String editedFinalText, String status,
            String tone, int versionNumber, UUID nextActionId,
            Instant createdAt, Instant copiedAt, Instant sentAt
    ) {}
}
