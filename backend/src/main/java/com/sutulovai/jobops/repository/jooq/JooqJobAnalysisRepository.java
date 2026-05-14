package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.JobAnalysisRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqJobAnalysisRepository implements JobAnalysisRepository {

    private final DSLContext dsl;

    public JooqJobAnalysisRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<AnalysisRow> findByVacancyId(UUID vacancyId) {
        return dsl.selectFrom(table("job_analyses"))
                .where(field("vacancy_id").eq(vacancyId))
                .fetchOptional(this::toRow);
    }

    @Override
    public AnalysisRow save(AnalysisRow a) {
        var id = a.id() != null ? a.id() : UUID.randomUUID();
        dsl.insertInto(table("job_analyses"))
                .set(field("id"), id)
                .set(field("vacancy_id"), a.vacancyId())
                .set(field("recommendation"), a.recommendation())
                .set(field("fit_score"), a.fitScore())
                .set(field("confidence"), a.confidence())
                .set(field("summary"), a.summary())
                .set(field("reasons_to_apply"), a.reasonsToApply() != null ? a.reasonsToApply() : new String[0])
                .set(field("reasons_to_skip"), a.reasonsToSkip() != null ? a.reasonsToSkip() : new String[0])
                .set(field("red_flags"), a.redFlags() != null ? a.redFlags() : new String[0])
                .set(field("uncertainties"), a.uncertainties() != null ? a.uncertainties() : new String[0])
                .set(field("missing_info"), a.missingInfo() != null ? a.missingInfo() : new String[0])
                .set(field("hard_blockers"), a.hardBlockers() != null ? a.hardBlockers() : new String[0])
                .set(field("role_fit"), a.roleFit())
                .set(field("stack_fit"), a.stackFit())
                .set(field("domain_fit"), a.domainFit())
                .set(field("seniority_fit"), a.seniorityFit())
                .set(field("location_fit"), a.locationFit())
                .set(field("language_fit"), a.languageFit())
                .set(field("company_type_fit"), a.companyTypeFit())
                .set(field("german_requirement"), a.germanRequirement())
                .set(field("relocation_risk"), a.relocationRisk())
                .set(field("salary_risk"), a.salaryRisk())
                .set(field("freshness_risk"), a.freshnessRisk())
                .set(field("suggested_positioning"), a.suggestedPositioning())
                .set(field("suggested_outreach_angle"), a.suggestedOutreachAngle())
                .set(field("suggested_salary_strategy"), a.suggestedSalaryStrategy())
                .set(field("suggested_first_message"), a.suggestedFirstMessage())
                .set(field("suggested_next_action"), a.suggestedNextAction())
                .set(field("suggested_priority"), a.suggestedPriority())
                .set(field("ai_model"), a.aiModel())
                .set(field("ai_tokens_used"), a.aiTokensUsed())
                .onConflict(field("vacancy_id"))
                .doUpdate()
                .set(field("recommendation"), a.recommendation())
                .set(field("fit_score"), a.fitScore())
                .set(field("confidence"), a.confidence())
                .set(field("summary"), a.summary())
                .set(field("reasons_to_apply"), a.reasonsToApply() != null ? a.reasonsToApply() : new String[0])
                .set(field("reasons_to_skip"), a.reasonsToSkip() != null ? a.reasonsToSkip() : new String[0])
                .set(field("red_flags"), a.redFlags() != null ? a.redFlags() : new String[0])
                .set(field("uncertainties"), a.uncertainties() != null ? a.uncertainties() : new String[0])
                .set(field("missing_info"), a.missingInfo() != null ? a.missingInfo() : new String[0])
                .set(field("hard_blockers"), a.hardBlockers() != null ? a.hardBlockers() : new String[0])
                .set(field("role_fit"), a.roleFit())
                .set(field("stack_fit"), a.stackFit())
                .set(field("domain_fit"), a.domainFit())
                .set(field("seniority_fit"), a.seniorityFit())
                .set(field("location_fit"), a.locationFit())
                .set(field("language_fit"), a.languageFit())
                .set(field("company_type_fit"), a.companyTypeFit())
                .set(field("german_requirement"), a.germanRequirement())
                .set(field("relocation_risk"), a.relocationRisk())
                .set(field("salary_risk"), a.salaryRisk())
                .set(field("freshness_risk"), a.freshnessRisk())
                .set(field("suggested_positioning"), a.suggestedPositioning())
                .set(field("suggested_outreach_angle"), a.suggestedOutreachAngle())
                .set(field("suggested_salary_strategy"), a.suggestedSalaryStrategy())
                .set(field("suggested_first_message"), a.suggestedFirstMessage())
                .set(field("suggested_next_action"), a.suggestedNextAction())
                .set(field("suggested_priority"), a.suggestedPriority())
                .set(field("ai_model"), a.aiModel())
                .set(field("ai_tokens_used"), a.aiTokensUsed())
                .execute();
        return findByVacancyId(a.vacancyId()).orElseThrow();
    }

    private AnalysisRow toRow(Record r) {
        return new AnalysisRow(
                r.get(field("id", UUID.class)),
                r.get(field("vacancy_id", UUID.class)),
                r.get(field("recommendation", String.class)),
                r.get(field("fit_score", Integer.class)),
                r.get(field("confidence", Integer.class)),
                r.get(field("summary", String.class)),
                r.get(field("reasons_to_apply", String[].class)),
                r.get(field("reasons_to_skip", String[].class)),
                r.get(field("red_flags", String[].class)),
                r.get(field("uncertainties", String[].class)),
                r.get(field("missing_info", String[].class)),
                r.get(field("hard_blockers", String[].class)),
                r.get(field("role_fit", Integer.class)),
                r.get(field("stack_fit", Integer.class)),
                r.get(field("domain_fit", Integer.class)),
                r.get(field("seniority_fit", Integer.class)),
                r.get(field("location_fit", Integer.class)),
                r.get(field("language_fit", Integer.class)),
                r.get(field("company_type_fit", Integer.class)),
                r.get(field("german_requirement", String.class)),
                r.get(field("relocation_risk", String.class)),
                r.get(field("salary_risk", String.class)),
                r.get(field("freshness_risk", String.class)),
                r.get(field("suggested_positioning", String.class)),
                r.get(field("suggested_outreach_angle", String.class)),
                r.get(field("suggested_salary_strategy", String.class)),
                r.get(field("suggested_first_message", String.class)),
                r.get(field("suggested_next_action", String.class)),
                r.get(field("suggested_priority", Integer.class)),
                r.get(field("ai_model", String.class)),
                r.get(field("ai_tokens_used", Integer.class)),
                RecordTimes.instantUtcOrNow(r, "created_at")
        );
    }
}
