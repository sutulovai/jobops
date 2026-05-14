package com.sutulovai.jobops.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record WeeklyReviewResponse(
        String id,
        int weekNumber,
        String weekStart,
        String weekEnd,
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
        int staleApplications,
        int ghostedApplications,
        int salaryBlockerCount,
        int languageBlockerCount,
        int relocationBlockerCount,
        String aiSummary,
        String whatWorked,
        String whatDidntWork,
        String aiRecommendations,
        String nextWeekTargets,
        List<String> aiCityRecommendations,
        String createdAt
) {}
