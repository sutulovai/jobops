package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.OutreachMessageRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqOutreachMessageRepository implements OutreachMessageRepository {

    private final DSLContext dsl;

    public JooqOutreachMessageRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<MessageRow> findByUserId(UUID userId) {
        return dsl.selectFrom(table("outreach_messages"))
                .where(field("user_id").eq(userId))
                .orderBy(field("created_at").desc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<MessageRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("outreach_messages"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public MessageRow save(MessageRow m) {
        var id = m.id() != null ? m.id() : UUID.randomUUID();
        dsl.insertInto(table("outreach_messages"))
                .set(field("id"), id)
                .set(field("user_id"), m.userId())
                .set(field("contact_id"), m.contactId())
                .set(field("company_id"), m.companyId())
                .set(field("vacancy_id"), m.vacancyId())
                .set(field("application_id"), m.applicationId())
                .set(field("message_type"), m.messageType())
                .set(field("channel"), m.channel() != null ? m.channel() : "LINKEDIN")
                .set(field("recipient_type"), m.recipientType())
                .set(field("generated_text"), m.generatedText())
                .set(field("edited_final_text"), m.editedFinalText())
                .set(field("status"), m.status() != null ? m.status() : "DRAFT")
                .set(field("tone"), m.tone())
                .set(field("version_number"), m.versionNumber())
                .set(field("next_action_id"), m.nextActionId())
                .execute();
        return findById(id, m.userId()).orElseThrow();
    }

    @Override
    public void markCopied(UUID id, UUID userId) {
        dsl.update(table("outreach_messages"))
                .set(field("status"), "COPIED")
                .set(field("copied_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void markSent(UUID id, UUID userId) {
        dsl.update(table("outreach_messages"))
                .set(field("status"), "SENT")
                .set(field("sent_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    private MessageRow toRow(Record r) {
        return new MessageRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("contact_id", UUID.class)),
                r.get(field("company_id", UUID.class)),
                r.get(field("vacancy_id", UUID.class)),
                r.get(field("application_id", UUID.class)),
                r.get(field("message_type", String.class)),
                r.get(field("channel", String.class)),
                r.get(field("recipient_type", String.class)),
                r.get(field("generated_text", String.class)),
                r.get(field("edited_final_text", String.class)),
                r.get(field("status", String.class)),
                r.get(field("tone", String.class)),
                r.get(field("version_number", Integer.class)),
                r.get(field("next_action_id", UUID.class)),
                RecordTimes.instantUtcOrNull(r, "created_at"),
                RecordTimes.instantUtcOrNull(r, "copied_at"),
                RecordTimes.instantUtcOrNull(r, "sent_at")
        );
    }
}
