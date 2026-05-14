package com.sutulovai.jobops.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyReviewRepository {

    record Row(
            UUID id,
            UUID userId,
            int weekNumber,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            int jobsAnalyzed,
            int applyCount,
            int maybeCount,
            int skipCount,
            int directApplications,
            int applicationsTarget,
            int recruiterMessages,
            int managerMessages,
            int referralRequests,
            int followUpsSent,
            int totalResponses,
            BigDecimal directApplyResponseRate,
            BigDecimal warmOutreachResponseRate,
            BigDecimal recruiterScreenRate,
            BigDecimal techInterviewRate,
            int staleCount,
            int ghostedCount,
            int salaryBlockerCount,
            int languageBlockerCount,
            int relocationBlockerCount,
            String aiSummary,
            String aiWhatWorked,
            String aiWhatDidntWork,
            String aiRecommendations,
            String nextWeekTargets,
            String[] aiCityRecommendations,
            String createdAt
    ) {}

    Optional<Row> findLatestByUserId(UUID userId);

    List<Row> findAllByUserId(UUID userId);

    Row save(Row row);
}
