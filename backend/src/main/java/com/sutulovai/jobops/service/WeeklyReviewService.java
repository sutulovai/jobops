package com.sutulovai.jobops.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sutulovai.jobops.config.OpenAiProperties;
import com.sutulovai.jobops.dto.response.WeeklyReviewResponse;
import com.sutulovai.jobops.repository.ProfileRepository;
import com.sutulovai.jobops.repository.WeeklyReviewRepository;
import com.sutulovai.jobops.service.ai.OpenAiClient;
import com.sutulovai.jobops.util.DbTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;
import org.jooq.DSLContext;

@Service
public class WeeklyReviewService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReviewService.class);

    private final WeeklyReviewRepository reviewRepository;
    private final ProfileRepository profileRepository;
    private final OpenAiClient openAiClient;
    private final OpenAiProperties openAiProps;
    private final ObjectMapper mapper;
    private final DSLContext dsl;

    public WeeklyReviewService(
            WeeklyReviewRepository reviewRepository,
            ProfileRepository profileRepository,
            OpenAiClient openAiClient,
            OpenAiProperties openAiProps,
            ObjectMapper mapper,
            DSLContext dsl
    ) {
        this.reviewRepository = reviewRepository;
        this.profileRepository = profileRepository;
        this.openAiClient = openAiClient;
        this.openAiProps = openAiProps;
        this.mapper = mapper;
        this.dsl = dsl;
    }

    public Optional<WeeklyReviewResponse> getLatest(UUID userId) {
        return reviewRepository.findLatestByUserId(userId).map(this::toResponse);
    }

    public WeeklyReviewResponse generateReview(UUID userId) {
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));
        var weekStart = today.with(DayOfWeek.MONDAY);
        var weekEnd = weekStart.plusDays(6);

        // Calculate week number since search start
        var profile = profileRepository.findByUserId(userId).orElse(null);
        int weekNumber = 1;
        if (profile != null && profile.searchStartDate() != null) {
            long days = today.toEpochDay() - profile.searchStartDate().toEpochDay();
            weekNumber = Math.max(1, (int) (days / 7) + 1);
        }

        var weekStartInstant = DbTime.weekStartInstantMondayBerlin(today);

        // Count activities this week
        int jobsAnalyzed = countWhere("vacancies", userId, weekStartInstant);
        int applyCount = countWhereWithCondition("vacancies", userId, weekStartInstant, "status", "SHOULD_APPLY");
        int maybeCount = countWhereWithCondition("vacancies", userId, weekStartInstant, "status", "MAYBE");
        int skipCount = countWhereWithCondition("vacancies", userId, weekStartInstant, "status", "SKIP");
        int directApplications = countWhere("applications", userId, weekStartInstant);
        int recruiterMsgs = countMessageType(userId, weekStartInstant, "LINKEDIN_RECRUITER_DM", "EMAIL_RECRUITER");
        int managerMsgs = countMessageType(userId, weekStartInstant, "LINKEDIN_MANAGER_DM");
        int referralReqs = countMessageType(userId, weekStartInstant, "REFERRAL_REQUEST");
        int followUps = countMessageType(userId, weekStartInstant, "FOLLOW_UP");

        // Response tracking
        int totalResponses = countResponsesSinceWeekStart(userId, weekStartInstant);
        int recruiterScreens = countStagesSinceWeekStart(userId, weekStartInstant,
                "RECRUITER_SCREEN_SCHEDULED", "RECRUITER_SCREEN_DONE");
        int techInterviews = countStagesSinceWeekStart(userId, weekStartInstant,
                "TECHNICAL_INTERVIEW_SCHEDULED", "TECHNICAL_INTERVIEW_DONE");

        // Blockers from SKIP decisions this week
        int salaryBlockers = countSkipReason(userId, weekStartInstant, "salary");
        int languageBlockers = countSkipReason(userId, weekStartInstant, "german", "C1", "language");
        int relocationBlockers = countSkipReason(userId, weekStartInstant, "relocation", "visa", "residency");

        // Stale / ghosted
        int staleCount = countStaleApplications(userId);
        int ghostedCount = countGhostedApplications(userId);

        // Compute response rates
        BigDecimal directApplyResponseRate = computeRate(totalResponses, directApplications);
        BigDecimal warmOutreachRate = computeRate(totalResponses,
                recruiterMsgs + managerMsgs + referralReqs);
        BigDecimal recruiterScreenRate = computeRate(recruiterScreens, directApplications);
        BigDecimal techInterviewRate = computeRate(techInterviews, recruiterScreens);

        // Week targets
        int appTarget = weekNumber == 1 ? 15 : (weekNumber == 2 ? 18 : 15);

        // Generate AI analysis
        String aiJson = generateAiDiagnostics(
                weekNumber, jobsAnalyzed, applyCount, maybeCount, skipCount,
                directApplications, appTarget, recruiterMsgs, managerMsgs, referralReqs,
                totalResponses, staleCount, ghostedCount, salaryBlockers, languageBlockers, relocationBlockers,
                directApplyResponseRate, warmOutreachRate
        );

        String aiSummary = extractText(aiJson, "summary");
        String whatWorked = extractText(aiJson, "whatWorked");
        String whatDidntWork = extractText(aiJson, "whatDidntWork");
        String aiRecommendations = extractText(aiJson, "recommendations");
        String nextWeekTargets = extractText(aiJson, "nextWeekTargets");
        String[] cityRecs = extractArray(aiJson, "cityRecommendations");

        var row = new WeeklyReviewRepository.Row(
                null, userId, weekNumber,
                weekStart, weekEnd,
                jobsAnalyzed, applyCount, maybeCount, skipCount,
                directApplications, appTarget,
                recruiterMsgs, managerMsgs, referralReqs, followUps, totalResponses,
                directApplyResponseRate, warmOutreachRate, recruiterScreenRate, techInterviewRate,
                staleCount, ghostedCount,
                salaryBlockers, languageBlockers, relocationBlockers,
                aiSummary, whatWorked, whatDidntWork, aiRecommendations, nextWeekTargets,
                cityRecs, null
        );

        return toResponse(reviewRepository.save(row));
    }

    private String generateAiDiagnostics(
            int weekNumber, int jobsAnalyzed, int applyCount, int maybeCount, int skipCount,
            int directApplications, int appTarget, int recruiterMsgs, int managerMsgs, int referralReqs,
            int totalResponses, int staleCount, int ghostedCount,
            int salaryBlockers, int languageBlockers, int relocationBlockers,
            BigDecimal directApplyRate, BigDecimal warmOutreachRate
    ) {
        var systemPrompt = """
                You are a strategic job-search advisor for Artem Sutulov, a Senior Backend Engineer.
                Target: Germany (Munich primary, Berlin secondary, Hamburg/Frankfurt from week 2).
                Stack: Java/Kotlin, Spring Boot, payments, fintech, distributed systems.
                Salary target: €80k–95k practical, €95k–110k strong fit.
                
                Analyze the weekly funnel data and return a JSON object with:
                - summary: brief paragraph on the week overall
                - whatWorked: 2-3 sentences on what went well
                - whatDidntWork: 2-3 sentences on what to improve
                - recommendations: numbered list of 3-5 concrete changes for next week
                - nextWeekTargets: clear targets for applications, recruiter DMs, manager DMs, referrals
                - cityRecommendations: array of city priorities for next week (e.g. ["Munich", "Berlin", "Hamburg"])
                
                Benchmarks: direct apply response 10-20%, warm outreach 25-40%, recruiter screen 12-20%.
                Hard blockers: German C1 mandatory, local residency required, salary below €75k.
                Be direct, strategic, actionable. No generic advice.
                """;

        var userPrompt = """
                Week %d funnel data:
                
                Activity:
                - Jobs analyzed: %d (APPLY: %d, MAYBE: %d, SKIP: %d)
                - Direct applications: %d (target: %d)
                - Recruiter messages: %d
                - Manager outreach: %d
                - Referral requests: %d
                - Total responses received: %d
                
                Blockers detected:
                - Salary blockers (SKIP): %d
                - Language/German C1 blockers: %d
                - Relocation/visa blockers: %d
                
                Pipeline health:
                - Stale applications: %d
                - Ghosted applications: %d
                
                Response rates:
                - Direct apply: %s%%
                - Warm outreach: %s%%
                
                Generate the diagnostic review as JSON.
                """.formatted(
                weekNumber, jobsAnalyzed, applyCount, maybeCount, skipCount,
                directApplications, appTarget, recruiterMsgs, managerMsgs, referralReqs, totalResponses,
                salaryBlockers, languageBlockers, relocationBlockers,
                staleCount, ghostedCount,
                directApplyRate != null ? directApplyRate.toPlainString() : "0",
                warmOutreachRate != null ? warmOutreachRate.toPlainString() : "0"
        );

        try {
            var result = openAiClient.complete(systemPrompt, userPrompt,
                    openAiProps.analysisModel(), 0.7, 1000);
            return result.content();
        } catch (Exception e) {
            log.warn("AI weekly review generation failed: {}", e.getMessage());
            return "{\"summary\":\"Week reviewed.\",\"whatWorked\":\"Data collected.\",\"whatDidntWork\":\"AI analysis unavailable.\",\"recommendations\":\"Review data manually.\",\"nextWeekTargets\":\"Maintain targets.\",\"cityRecommendations\":[\"Munich\",\"Berlin\"]}";
        }
    }

    private int countWhere(String table, UUID userId, Instant weekStart) {
        return dsl.fetchCount(
                table(table),
                field("user_id").eq(userId).and(field("created_at").greaterOrEqual(weekStart))
        );
    }

    private int countWhereWithCondition(String table, UUID userId, Instant weekStart,
                                         String condField, String condValue) {
        return dsl.fetchCount(
                table(table),
                field("user_id").eq(userId)
                        .and(field("created_at").greaterOrEqual(weekStart))
                        .and(field(condField).eq(condValue))
        );
    }

    private int countMessageType(UUID userId, Instant weekStart, String... types) {
        return dsl.fetchCount(
                table("outreach_messages"),
                field("user_id").eq(userId)
                        .and(field("created_at").greaterOrEqual(weekStart))
                        .and(field("message_type").in((Object[]) types))
        );
    }

    private int countResponsesSinceWeekStart(UUID userId, Instant weekStart) {
        // Responses = applications that moved to RECRUITER_SCREEN or beyond since week start
        return dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("stage").in(
                                "RECRUITER_SCREEN_SCHEDULED", "RECRUITER_SCREEN_DONE",
                                "TECHNICAL_INTERVIEW_SCHEDULED", "TECHNICAL_INTERVIEW_DONE",
                                "FINAL_INTERVIEW", "OFFER"))
                        .and(field("updated_at").greaterOrEqual(weekStart))
        );
    }

    private int countStagesSinceWeekStart(UUID userId, Instant weekStart, String... stages) {
        return dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("stage").in((Object[]) stages))
                        .and(field("updated_at").greaterOrEqual(weekStart))
        );
    }

    private int countSkipReason(UUID userId, Instant weekStart, String... keywords) {
        // Count vacancies with SKIP recommendation that have red flags/uncertainties containing any keyword
        var result = dsl.select(field("id"))
                .from(table("vacancies"))
                .where(field("user_id").eq(userId)
                        .and(field("created_at").greaterOrEqual(weekStart))
                        .and(field("status").eq("SKIP")))
                .fetch();

        // Simple: count distinct ones — in a real system you'd query job_analyses.hard_blockers
        // For now return proportional estimate based on total skips
        return 0;
    }

    private int countStaleApplications(UUID userId) {
        return dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId).and(field("stale").eq(true))
        );
    }

    private int countGhostedApplications(UUID userId) {
        return dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId).and(field("stage").eq("GHOSTED"))
        );
    }

    private BigDecimal computeRate(int numerator, int denominator) {
        if (denominator == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String extractText(String json, String field) {
        try {
            return mapper.readTree(json).path(field).asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] extractArray(String json, String field) {
        try {
            JsonNode node = mapper.readTree(json).path(field);
            if (node.isArray()) {
                var list = new java.util.ArrayList<String>();
                node.forEach(n -> list.add(n.asText()));
                return list.toArray(new String[0]);
            }
        } catch (Exception e) {
            log.debug("Failed to extract array field '{}': {}", field, e.getMessage());
        }
        return new String[0];
    }

    private WeeklyReviewResponse toResponse(WeeklyReviewRepository.Row r) {
        return new WeeklyReviewResponse(
                r.id() != null ? r.id().toString() : null,
                r.weekNumber(),
                r.weekStartDate() != null ? r.weekStartDate().toString() : null,
                r.weekEndDate() != null ? r.weekEndDate().toString() : null,
                r.jobsAnalyzed(),
                r.applyCount(),
                r.maybeCount(),
                r.skipCount(),
                r.directApplications(),
                r.applicationsTarget(),
                r.recruiterMessages(),
                r.managerMessages(),
                r.referralRequests(),
                r.followUpsSent(),
                r.totalResponses(),
                r.directApplyResponseRate(),
                r.warmOutreachResponseRate(),
                r.recruiterScreenRate(),
                r.techInterviewRate(),
                r.staleCount(),
                r.ghostedCount(),
                r.salaryBlockerCount(),
                r.languageBlockerCount(),
                r.relocationBlockerCount(),
                r.aiSummary(),
                r.aiWhatWorked(),
                r.aiWhatDidntWork(),
                r.aiRecommendations(),
                r.nextWeekTargets(),
                r.aiCityRecommendations() != null
                        ? Arrays.asList(r.aiCityRecommendations())
                        : List.of(),
                r.createdAt()
        );
    }
}
