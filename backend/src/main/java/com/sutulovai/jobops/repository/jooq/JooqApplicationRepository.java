package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.ApplicationRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqApplicationRepository implements ApplicationRepository {

    private final DSLContext dsl;

    public JooqApplicationRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<ApplicationRow> findByUserId(UUID userId) {
        return dsl.selectFrom(table("applications"))
                .where(field("user_id").eq(userId))
                .orderBy(field("priority").desc(), field("created_at").desc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<ApplicationRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("applications"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public Optional<ApplicationRow> findByVacancyId(UUID vacancyId, UUID userId) {
        return dsl.selectFrom(table("applications"))
                .where(field("vacancy_id").eq(vacancyId).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public ApplicationRow save(ApplicationRow a) {
        var id = a.id() != null ? a.id() : UUID.randomUUID();
        dsl.insertInto(table("applications"))
                .set(field("id"), id)
                .set(field("user_id"), a.userId())
                .set(field("vacancy_id"), a.vacancyId())
                .set(field("company_id"), a.companyId())
                .set(field("cv_id"), a.cvId())
                .set(field("stage"), a.stage() != null ? a.stage() : "ADDED_TO_PIPELINE")
                .set(field("application_channel"), a.applicationChannel())
                .set(field("source_channel"), a.sourceChannel())
                .set(field("date_applied"), a.dateApplied())
                .set(field("recruiter_contacted"), a.recruiterContacted())
                .set(field("hiring_manager_contacted"), a.hiringManagerContacted())
                .set(field("referral_requested"), a.referralRequested())
                .set(field("follow_up_count"), a.followUpCount())
                .set(field("last_contact_date"), a.lastContactDate())
                .set(field("next_action_date"), a.nextActionDate())
                .set(field("priority"), a.priority())
                .set(field("stale"), a.stale())
                .set(field("notes"), a.notes())
                .set(field("outcome"), a.outcome())
                .set(field("rejection_reason"), a.rejectionReason())
                .set(field("city_category"), a.cityCategory())
                .onConflict(field("id"))
                .doUpdate()
                .set(field("cv_id"), a.cvId())
                .set(field("stage"), a.stage() != null ? a.stage() : "ADDED_TO_PIPELINE")
                .set(field("application_channel"), a.applicationChannel())
                .set(field("date_applied"), a.dateApplied())
                .set(field("recruiter_contacted"), a.recruiterContacted())
                .set(field("hiring_manager_contacted"), a.hiringManagerContacted())
                .set(field("referral_requested"), a.referralRequested())
                .set(field("follow_up_count"), a.followUpCount())
                .set(field("last_contact_date"), a.lastContactDate())
                .set(field("next_action_date"), a.nextActionDate())
                .set(field("priority"), a.priority())
                .set(field("stale"), a.stale())
                .set(field("notes"), a.notes())
                .set(field("outcome"), a.outcome())
                .set(field("rejection_reason"), a.rejectionReason())
                .set(field("city_category"), a.cityCategory())
                .set(field("updated_at"), now())
                .execute();
        return findById(id, a.userId()).orElseThrow();
    }

    @Override
    public void updateStage(UUID id, UUID userId, String stage) {
        dsl.update(table("applications"))
                .set(field("stage"), stage)
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void incrementFollowUp(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("follow_up_count"), field("follow_up_count", Integer.class).add(1))
                .set(field("last_contact_date"), currentLocalDate())
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void markStale(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("stale"), true)
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void setRecruiterContacted(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("recruiter_contacted"), true)
                .set(field("last_contact_date"), currentLocalDate())
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void setHiringManagerContacted(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("hiring_manager_contacted"), true)
                .set(field("last_contact_date"), currentLocalDate())
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void setReferralRequested(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("referral_requested"), true)
                .set(field("last_contact_date"), currentLocalDate())
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void updateLastContactDate(UUID id, UUID userId) {
        dsl.update(table("applications"))
                .set(field("last_contact_date"), currentLocalDate())
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public List<ApplicationRow> findStaleApplications(UUID userId, int daysThreshold) {
        return dsl.selectFrom(table("applications"))
                .where(field("user_id").eq(userId))
                .and(field("stage").notIn("OFFER", "REJECTED", "GHOSTED", "WITHDRAWN", "ARCHIVED"))
                .and(field("updated_at").lessThan(Instant.now().minus(Duration.ofDays(daysThreshold))))
                .fetch(this::toRow);
    }

    private ApplicationRow toRow(Record r) {
        return new ApplicationRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("vacancy_id", UUID.class)),
                r.get(field("company_id", UUID.class)),
                r.get(field("cv_id", UUID.class)),
                r.get(field("stage", String.class)),
                r.get(field("application_channel", String.class)),
                r.get(field("source_channel", String.class)),
                RecordTimes.localDateOrNull(r, "date_applied"),
                r.get(field("recruiter_contacted", Boolean.class)),
                r.get(field("hiring_manager_contacted", Boolean.class)),
                r.get(field("referral_requested", Boolean.class)),
                r.get(field("follow_up_count", Integer.class)),
                RecordTimes.localDateOrNull(r, "last_contact_date"),
                RecordTimes.localDateOrNull(r, "next_action_date"),
                r.get(field("priority", Integer.class)),
                r.get(field("stale", Boolean.class)),
                r.get(field("notes", String.class)),
                r.get(field("outcome", String.class)),
                r.get(field("rejection_reason", String.class)),
                r.get(field("city_category", String.class)),
                RecordTimes.instantUtcOrNow(r, "created_at"),
                RecordTimes.instantUtcOrNow(r, "updated_at")
        );
    }
}
