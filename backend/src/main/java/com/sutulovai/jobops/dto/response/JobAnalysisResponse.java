package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobAnalysisResponse(
        UUID id,
        UUID vacancyId,
        String recommendation,
        int fitScore,
        int confidence,
        String summary,
        List<String> reasonsToApply,
        List<String> reasonsToSkip,
        List<String> redFlags,
        List<String> uncertainties,
        List<String> missingInfo,
        List<String> hardBlockers,
        Integer roleFit,
        Integer stackFit,
        Integer domainFit,
        Integer seniorityFit,
        Integer locationFit,
        Integer languageFit,
        Integer companyTypeFit,
        String germanRequirement,
        String relocationRisk,
        String salaryRisk,
        String freshnessRisk,
        String suggestedPositioning,
        String suggestedOutreachAngle,
        String suggestedSalaryStrategy,
        String suggestedFirstMessage,
        String suggestedNextAction,
        Integer suggestedPriority,
        Instant createdAt
) {
}
