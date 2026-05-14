package com.sutulovai.jobops.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        int todayActionsCount,
        int overdueActionsCount,
        int pendingDecisionsCount,
        int activeApplicationsCount,
        Map<String, Integer> applicationsByStage,
        WeeklyProgressResponse weeklyProgress,
        Integer responseRatePercent,
        List<NextActionResponse> topPriorityActions
) {
    public record WeeklyProgressResponse(
            int weekNumber,
            int applicationsTarget,
            int applicationsDone,
            int recruiterMessagesTarget,
            int recruiterMessagesDone,
            int managerMessagesTarget,
            int managerMessagesDone,
            int referralRequestsTarget,
            int referralRequestsDone,
            int jobsAnalyzedTarget,
            int jobsAnalyzedDone
    ) {}
}
