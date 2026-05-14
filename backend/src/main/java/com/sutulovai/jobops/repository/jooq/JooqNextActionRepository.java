package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.NextActionRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqNextActionRepository implements NextActionRepository {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private final DSLContext dsl;

    public JooqNextActionRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<ActionRow> findTodayAndOverdue(UUID userId) {
        var today = LocalDate.now(BERLIN);
        return dsl.selectFrom(table("next_actions"))
                .where(field("user_id").eq(userId))
                .and(field("status").eq("PENDING"))
                .and(field("due_date").lessOrEqual(today))
                .and(field("skipped_until").isNull().or(field("skipped_until").lessOrEqual(today)))
                .orderBy(field("priority_score").desc(), field("due_date").asc())
                .fetch(this::toRow);
    }

    @Override
    public List<ActionRow> findPending(UUID userId) {
        var today = LocalDate.now(BERLIN);
        return dsl.selectFrom(table("next_actions"))
                .where(field("user_id").eq(userId))
                .and(field("status").eq("PENDING"))
                .and(field("skipped_until").isNull().or(field("skipped_until").lessOrEqual(today)))
                .orderBy(field("priority_score").desc(), field("due_date").asc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<ActionRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("next_actions"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public ActionRow save(ActionRow a) {
        var id = a.id() != null ? a.id() : UUID.randomUUID();
        dsl.insertInto(table("next_actions"))
                .set(field("id"), id)
                .set(field("user_id"), a.userId())
                .set(field("action_type"), a.actionType())
                .set(field("priority"), a.priority() != null ? a.priority() : "P2")
                .set(field("priority_score"), a.priorityScore())
                .set(field("due_date"), a.dueDate())
                .set(field("status"), a.status() != null ? a.status() : "PENDING")
                .set(field("reason"), a.reason())
                .set(field("company_id"), a.companyId())
                .set(field("vacancy_id"), a.vacancyId())
                .set(field("application_id"), a.applicationId())
                .set(field("contact_id"), a.contactId())
                .set(field("message_id"), a.messageId())
                .set(field("saved_search_id"), a.savedSearchId())
                .set(field("generated_message_required"), a.generatedMessageRequired())
                .set(field("recommended_message_type"), a.recommendedMessageType())
                .set(field("snoozed_until"), a.snoozedUntil())
                .set(field("skipped_until"), a.skippedUntil())
                .execute();
        return findById(id, a.userId()).orElseThrow();
    }

    @Override
    public void markDone(UUID id, UUID userId) {
        dsl.update(table("next_actions"))
                .set(field("status"), "DONE")
                .set(field("skipped_until"), (LocalDate) null)
                .set(field("completed_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void skip(UUID id, UUID userId) {
        var resumeOn = LocalDate.now(BERLIN).plusDays(1);
        dsl.update(table("next_actions"))
                .set(field("skipped_until"), resumeOn)
                .set(field("status"), "PENDING")
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void snooze(UUID id, UUID userId, String snoozeUntil) {
        dsl.update(table("next_actions"))
                .set(field("status"), "SNOOZED")
                .set(field("snoozed_until"), snoozeUntil)
                .set(field("skipped_until"), (LocalDate) null)
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void markObsolete(UUID id, UUID userId) {
        dsl.update(table("next_actions"))
                .set(field("status"), "OBSOLETE")
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public boolean existsPendingAction(
            UUID userId,
            String actionType,
            UUID companyId,
            UUID vacancyId,
            UUID applicationId,
            UUID contactId,
            UUID savedSearchId
    ) {
        var condition = field("user_id").eq(userId)
                .and(field("action_type").eq(actionType))
                .and(field("status").eq("PENDING"));
        if (applicationId != null) {
            condition = condition.and(field("application_id").eq(applicationId));
        } else if (vacancyId != null) {
            condition = condition.and(field("vacancy_id").eq(vacancyId));
        } else if (contactId != null) {
            condition = condition.and(field("contact_id").eq(contactId));
        } else if (savedSearchId != null) {
            condition = condition.and(field("saved_search_id").eq(savedSearchId));
        } else if (companyId != null) {
            condition = condition.and(field("company_id").eq(companyId));
        }
        return dsl.fetchCount(table("next_actions"), condition) > 0;
    }

    @Override
    public long countByTypeAndStatus(UUID userId, String actionType, String status, String since) {
        var condition = field("user_id").eq(userId)
                .and(field("action_type").eq(actionType))
                .and(field("status").eq(status));
        if (since != null) {
            condition = condition.and(field("created_at").greaterOrEqual(since));
        }
        return dsl.fetchCount(table("next_actions"), condition);
    }

    private ActionRow toRow(Record r) {
        return new ActionRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("action_type", String.class)),
                r.get(field("priority", String.class)),
                r.get(field("priority_score", Integer.class)),
                optString(r, "due_date"),
                r.get(field("status", String.class)),
                r.get(field("reason", String.class)),
                r.get(field("company_id", UUID.class)),
                r.get(field("vacancy_id", UUID.class)),
                r.get(field("application_id", UUID.class)),
                r.get(field("contact_id", UUID.class)),
                r.get(field("message_id", UUID.class)),
                r.get(field("saved_search_id", UUID.class)),
                r.get(field("generated_message_required", Boolean.class)),
                r.get(field("recommended_message_type", String.class)),
                optString(r, "snoozed_until"),
                optString(r, "skipped_until"),
                RecordTimes.instantUtcOrNull(r, "created_at"),
                RecordTimes.instantUtcOrNull(r, "completed_at")
        );
    }

    private static String optString(Record r, String fieldName) {
        var val = r.get(field(fieldName));
        return val != null ? val.toString() : null;
    }
}
