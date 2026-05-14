package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.WeeklyReviewRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqWeeklyReviewRepository implements WeeklyReviewRepository {

    private final DSLContext dsl;

    public JooqWeeklyReviewRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<Row> findLatestByUserId(UUID userId) {
        return dsl.selectFrom(table("weekly_reviews"))
                .where(field("user_id").eq(userId))
                .orderBy(field("week_number").desc())
                .limit(1)
                .fetchOptional(this::toRow);
    }

    @Override
    public List<Row> findAllByUserId(UUID userId) {
        return dsl.selectFrom(table("weekly_reviews"))
                .where(field("user_id").eq(userId))
                .orderBy(field("week_number").desc())
                .fetch(this::toRow);
    }

    @Override
    public Row save(Row row) {
        var id = row.id() != null ? row.id() : UUID.randomUUID();
        var weekEnd = row.weekEndDate() != null ? row.weekEndDate()
                : (row.weekStartDate() != null ? row.weekStartDate().plusDays(6) : LocalDate.now().plusDays(6));

        dsl.insertInto(table("weekly_reviews"))
                .set(field("id"), id)
                .set(field("user_id"), row.userId())
                .set(field("week_number"), row.weekNumber())
                .set(field("week_start_date"), row.weekStartDate())
                .set(field("week_end_date"), weekEnd)
                .set(field("jobs_analyzed"), row.jobsAnalyzed())
                .set(field("apply_count"), row.applyCount())
                .set(field("maybe_count"), row.maybeCount())
                .set(field("skip_count"), row.skipCount())
                .set(field("direct_applications"), row.directApplications())
                .set(field("applications_target"), row.applicationsTarget())
                .set(field("recruiter_messages"), row.recruiterMessages())
                .set(field("manager_messages"), row.managerMessages())
                .set(field("referral_requests"), row.referralRequests())
                .set(field("follow_ups_sent"), row.followUpsSent())
                .set(field("total_responses"), row.totalResponses())
                .set(field("direct_apply_response_rate"), row.directApplyResponseRate())
                .set(field("warm_outreach_response_rate"), row.warmOutreachResponseRate())
                .set(field("recruiter_screen_rate"), row.recruiterScreenRate())
                .set(field("tech_interview_rate"), row.techInterviewRate())
                .set(field("stale_count"), row.staleCount())
                .set(field("ghosted_count"), row.ghostedCount())
                .set(field("salary_blocker_count"), row.salaryBlockerCount())
                .set(field("language_blocker_count"), row.languageBlockerCount())
                .set(field("relocation_blocker_count"), row.relocationBlockerCount())
                .set(field("ai_summary"), row.aiSummary())
                .set(field("ai_what_worked"), row.aiWhatWorked())
                .set(field("ai_what_didnt_work"), row.aiWhatDidntWork())
                .set(field("ai_recommendations"), row.aiRecommendations())
                .set(field("next_week_targets"), row.nextWeekTargets())
                .set(field("ai_city_recommendations"),
                        row.aiCityRecommendations() != null ? row.aiCityRecommendations() : new String[0])
                .onConflict(field("user_id"), field("week_number"))
                .doUpdate()
                .set(field("jobs_analyzed"), row.jobsAnalyzed())
                .set(field("apply_count"), row.applyCount())
                .set(field("maybe_count"), row.maybeCount())
                .set(field("skip_count"), row.skipCount())
                .set(field("direct_applications"), row.directApplications())
                .set(field("applications_target"), row.applicationsTarget())
                .set(field("recruiter_messages"), row.recruiterMessages())
                .set(field("manager_messages"), row.managerMessages())
                .set(field("referral_requests"), row.referralRequests())
                .set(field("follow_ups_sent"), row.followUpsSent())
                .set(field("total_responses"), row.totalResponses())
                .set(field("direct_apply_response_rate"), row.directApplyResponseRate())
                .set(field("warm_outreach_response_rate"), row.warmOutreachResponseRate())
                .set(field("recruiter_screen_rate"), row.recruiterScreenRate())
                .set(field("tech_interview_rate"), row.techInterviewRate())
                .set(field("stale_count"), row.staleCount())
                .set(field("ghosted_count"), row.ghostedCount())
                .set(field("salary_blocker_count"), row.salaryBlockerCount())
                .set(field("language_blocker_count"), row.languageBlockerCount())
                .set(field("relocation_blocker_count"), row.relocationBlockerCount())
                .set(field("ai_summary"), row.aiSummary())
                .set(field("ai_what_worked"), row.aiWhatWorked())
                .set(field("ai_what_didnt_work"), row.aiWhatDidntWork())
                .set(field("ai_recommendations"), row.aiRecommendations())
                .set(field("next_week_targets"), row.nextWeekTargets())
                .set(field("ai_city_recommendations"),
                        row.aiCityRecommendations() != null ? row.aiCityRecommendations() : new String[0])
                .execute();

        return findLatestByUserId(row.userId()).orElseThrow();
    }

    private Row toRow(Record r) {
        String[] cityRecs = r.get(field("ai_city_recommendations", String[].class));

        return new Row(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("week_number", Integer.class)),
                r.get(field("week_start_date", LocalDate.class)),
                r.get(field("week_end_date", LocalDate.class)),
                nvl(r.get(field("jobs_analyzed", Integer.class)), 0),
                nvl(r.get(field("apply_count", Integer.class)), 0),
                nvl(r.get(field("maybe_count", Integer.class)), 0),
                nvl(r.get(field("skip_count", Integer.class)), 0),
                nvl(r.get(field("direct_applications", Integer.class)), 0),
                nvl(r.get(field("applications_target", Integer.class)), 0),
                nvl(r.get(field("recruiter_messages", Integer.class)), 0),
                nvl(r.get(field("manager_messages", Integer.class)), 0),
                nvl(r.get(field("referral_requests", Integer.class)), 0),
                nvl(r.get(field("follow_ups_sent", Integer.class)), 0),
                nvl(r.get(field("total_responses", Integer.class)), 0),
                r.get(field("direct_apply_response_rate", BigDecimal.class)),
                r.get(field("warm_outreach_response_rate", BigDecimal.class)),
                r.get(field("recruiter_screen_rate", BigDecimal.class)),
                r.get(field("tech_interview_rate", BigDecimal.class)),
                nvl(r.get(field("stale_count", Integer.class)), 0),
                nvl(r.get(field("ghosted_count", Integer.class)), 0),
                nvl(r.get(field("salary_blocker_count", Integer.class)), 0),
                nvl(r.get(field("language_blocker_count", Integer.class)), 0),
                nvl(r.get(field("relocation_blocker_count", Integer.class)), 0),
                r.get(field("ai_summary", String.class)),
                r.get(field("ai_what_worked", String.class)),
                r.get(field("ai_what_didnt_work", String.class)),
                r.get(field("ai_recommendations", String.class)),
                r.get(field("next_week_targets", String.class)),
                cityRecs != null ? cityRecs : new String[0],
                r.get(field("created_at")).toString()
        );
    }

    private static int nvl(Integer value, int fallback) {
        return value != null ? value : fallback;
    }
}
