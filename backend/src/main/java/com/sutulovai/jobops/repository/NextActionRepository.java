package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NextActionRepository {
    List<ActionRow> findTodayAndOverdue(UUID userId);
    List<ActionRow> findPending(UUID userId);
    Optional<ActionRow> findById(UUID id, UUID userId);
    ActionRow save(ActionRow action);
    void markDone(UUID id, UUID userId);
    void skip(UUID id, UUID userId);
    void snooze(UUID id, UUID userId, String snoozeUntil);
    void markObsolete(UUID id, UUID userId);
    boolean existsPendingAction(
            UUID userId,
            String actionType,
            UUID companyId,
            UUID vacancyId,
            UUID applicationId,
            UUID contactId,
            UUID savedSearchId
    );
    long countByTypeAndStatus(UUID userId, String actionType, String status, String since);

    record ActionRow(
            UUID id, UUID userId, String actionType, String priority, int priorityScore,
            String dueDate, String status, String reason,
            UUID companyId, UUID vacancyId, UUID applicationId,
            UUID contactId, UUID messageId, UUID savedSearchId,
            boolean generatedMessageRequired, String recommendedMessageType,
            String snoozedUntil, String skippedUntil, Instant createdAt, Instant completedAt
    ) {}
}
