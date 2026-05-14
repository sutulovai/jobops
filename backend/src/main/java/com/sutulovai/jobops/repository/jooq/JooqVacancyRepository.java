package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.VacancyRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqVacancyRepository implements VacancyRepository {

    private final DSLContext dsl;

    public JooqVacancyRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<VacancyRow> findByUserId(UUID userId, String status) {
        var query = dsl.selectFrom(table("vacancies"))
                .where(field("user_id").eq(userId));
        if (status != null && !status.isBlank()) {
            query = query.and(field("status").eq(status));
        }
        return query.orderBy(field("created_at").desc()).fetch(this::toRow);
    }

    @Override
    public Optional<VacancyRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("vacancies"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public VacancyRow save(VacancyRow v) {
        var id = v.id() != null ? v.id() : UUID.randomUUID();
        dsl.insertInto(table("vacancies"))
                .set(field("id"), id)
                .set(field("user_id"), v.userId())
                .set(field("company_id"), v.companyId())
                .set(field("title"), v.title())
                .set(field("location"), v.location())
                .set(field("remote_policy"), v.remotePolicy())
                .set(field("url"), v.url())
                .set(field("source_channel"), v.sourceChannel())
                .set(field("job_description_text"), v.jobDescriptionText())
                .set(field("stack_keywords"), v.stackKeywords() != null ? v.stackKeywords() : new String[0])
                .set(field("domain_keywords"), v.domainKeywords() != null ? v.domainKeywords() : new String[0])
                .set(field("salary_range_min"), v.salaryRangeMin())
                .set(field("salary_range_max"), v.salaryRangeMax())
                .set(field("salary_currency"), v.salaryCurrency() != null ? v.salaryCurrency() : "EUR")
                .set(field("language_requirement"), v.languageRequirement())
                .set(field("relocation_visa_wording"), v.relocationVisaWording())
                .set(field("seniority"), v.seniority())
                .set(field("employment_type"), v.employmentType())
                .set(field("status"), v.status() != null ? v.status() : "DISCOVERED")
                .set(field("ai_fit_score"), v.aiFitScore())
                .set(field("ai_confidence"), v.aiConfidence())
                .set(field("ai_recommendation"), v.aiRecommendation())
                .set(field("ai_reasoning"), v.aiReasoning())
                .set(field("red_flags"), v.redFlags() != null ? v.redFlags() : new String[0])
                .set(field("uncertainty_flags"), v.uncertaintyFlags() != null ? v.uncertaintyFlags() : new String[0])
                .set(field("discovered_date"), v.discoveredDate())
                .onConflict(field("id"))
                .doUpdate()
                .set(field("company_id"), v.companyId())
                .set(field("title"), v.title())
                .set(field("location"), v.location())
                .set(field("remote_policy"), v.remotePolicy())
                .set(field("url"), v.url())
                .set(field("source_channel"), v.sourceChannel())
                .set(field("job_description_text"), v.jobDescriptionText())
                .set(field("stack_keywords"), v.stackKeywords() != null ? v.stackKeywords() : new String[0])
                .set(field("domain_keywords"), v.domainKeywords() != null ? v.domainKeywords() : new String[0])
                .set(field("salary_range_min"), v.salaryRangeMin())
                .set(field("salary_range_max"), v.salaryRangeMax())
                .set(field("language_requirement"), v.languageRequirement())
                .set(field("relocation_visa_wording"), v.relocationVisaWording())
                .set(field("seniority"), v.seniority())
                .set(field("employment_type"), v.employmentType())
                .set(field("status"), v.status() != null ? v.status() : "DISCOVERED")
                .set(field("ai_fit_score"), v.aiFitScore())
                .set(field("ai_confidence"), v.aiConfidence())
                .set(field("ai_recommendation"), v.aiRecommendation())
                .set(field("ai_reasoning"), v.aiReasoning())
                .set(field("red_flags"), v.redFlags() != null ? v.redFlags() : new String[0])
                .set(field("uncertainty_flags"), v.uncertaintyFlags() != null ? v.uncertaintyFlags() : new String[0])
                .set(field("updated_at"), now())
                .execute();
        return findById(id, v.userId()).orElseThrow();
    }

    @Override
    public void updateStatus(UUID id, UUID userId, String status) {
        dsl.update(table("vacancies"))
                .set(field("status"), status)
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void delete(UUID id, UUID userId) {
        dsl.deleteFrom(table("vacancies"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    private VacancyRow toRow(Record r) {
        return new VacancyRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("company_id", UUID.class)),
                r.get(field("title", String.class)),
                r.get(field("location", String.class)),
                r.get(field("remote_policy", String.class)),
                r.get(field("url", String.class)),
                r.get(field("source_channel", String.class)),
                r.get(field("job_description_text", String.class)),
                r.get(field("stack_keywords", String[].class)),
                r.get(field("domain_keywords", String[].class)),
                r.get(field("salary_range_min", Integer.class)),
                r.get(field("salary_range_max", Integer.class)),
                r.get(field("salary_currency", String.class)),
                r.get(field("language_requirement", String.class)),
                r.get(field("relocation_visa_wording", String.class)),
                r.get(field("seniority", String.class)),
                r.get(field("employment_type", String.class)),
                r.get(field("status", String.class)),
                r.get(field("ai_fit_score", Integer.class)),
                r.get(field("ai_confidence", Integer.class)),
                r.get(field("ai_recommendation", String.class)),
                r.get(field("ai_reasoning", String.class)),
                r.get(field("red_flags", String[].class)),
                r.get(field("uncertainty_flags", String[].class)),
                RecordTimes.localDateOrNull(r, "discovered_date"),
                RecordTimes.instantUtcOrNow(r, "created_at"),
                RecordTimes.instantUtcOrNow(r, "updated_at")
        );
    }
}
