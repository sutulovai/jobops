package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.ContactRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqContactRepository implements ContactRepository {

    private final DSLContext dsl;

    public JooqContactRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<ContactRow> findByUserId(UUID userId) {
        return dsl.selectFrom(table("contacts"))
                .where(field("user_id").eq(userId))
                .orderBy(field("name").asc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<ContactRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("contacts"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public List<ContactRow> findByCompanyId(UUID companyId, UUID userId) {
        return dsl.selectFrom(table("contacts"))
                .where(field("company_id").eq(companyId).and(field("user_id").eq(userId)))
                .fetch(this::toRow);
    }

    @Override
    public ContactRow save(ContactRow c) {
        var id = c.id() != null ? c.id() : UUID.randomUUID();
        dsl.insertInto(table("contacts"))
                .set(field("id"), id)
                .set(field("user_id"), c.userId())
                .set(field("company_id"), c.companyId())
                .set(field("name"), c.name())
                .set(field("title"), c.title())
                .set(field("contact_type"), c.contactType() != null ? c.contactType() : "OTHER")
                .set(field("linked_in_url"), c.linkedInUrl())
                .set(field("email"), c.email())
                .set(field("relationship_strength"), c.relationshipStrength() != null ? c.relationshipStrength() : "COLD")
                .set(field("source"), c.source())
                .set(field("last_contacted_date"), c.lastContactedDate())
                .set(field("next_follow_up_date"), c.nextFollowUpDate())
                .set(field("notes"), c.notes())
                .set(field("preferred_channel"), c.preferredChannel() != null ? c.preferredChannel() : "LINKEDIN")
                .set(field("status"), c.status() != null ? c.status() : "NEW")
                .set(field("vacancy_id"), c.vacancyId())
                .set(field("application_id"), c.applicationId())
                .onConflict(field("id"))
                .doUpdate()
                .set(field("company_id"), c.companyId())
                .set(field("name"), c.name())
                .set(field("title"), c.title())
                .set(field("contact_type"), c.contactType() != null ? c.contactType() : "OTHER")
                .set(field("linked_in_url"), c.linkedInUrl())
                .set(field("email"), c.email())
                .set(field("relationship_strength"), c.relationshipStrength() != null ? c.relationshipStrength() : "COLD")
                .set(field("source"), c.source())
                .set(field("last_contacted_date"), c.lastContactedDate())
                .set(field("next_follow_up_date"), c.nextFollowUpDate())
                .set(field("notes"), c.notes())
                .set(field("preferred_channel"), c.preferredChannel() != null ? c.preferredChannel() : "LINKEDIN")
                .set(field("status"), c.status() != null ? c.status() : "NEW")
                .set(field("vacancy_id"), c.vacancyId())
                .set(field("application_id"), c.applicationId())
                .set(field("updated_at"), now())
                .execute();
        return findById(id, c.userId()).orElseThrow();
    }

    @Override
    public void delete(UUID id, UUID userId) {
        dsl.deleteFrom(table("contacts"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    private ContactRow toRow(Record r) {
        return new ContactRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("company_id", UUID.class)),
                r.get(field("name", String.class)),
                r.get(field("title", String.class)),
                r.get(field("contact_type", String.class)),
                r.get(field("linked_in_url", String.class)),
                r.get(field("email", String.class)),
                r.get(field("relationship_strength", String.class)),
                r.get(field("source", String.class)),
                optStr(r, "last_contacted_date"),
                optStr(r, "next_follow_up_date"),
                r.get(field("notes", String.class)),
                r.get(field("preferred_channel", String.class)),
                r.get(field("status", String.class)),
                r.get(field("vacancy_id", UUID.class)),
                r.get(field("application_id", UUID.class)),
                RecordTimes.instantUtcOrNow(r, "created_at"),
                RecordTimes.instantUtcOrNow(r, "updated_at")
        );
    }

    private static String optStr(Record r, String f) {
        var v = r.get(field(f));
        return v != null ? v.toString() : null;
    }
}
