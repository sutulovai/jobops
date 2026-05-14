package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobAnalysisRepository {
    Optional<AnalysisRow> findByVacancyId(UUID vacancyId);
    AnalysisRow save(AnalysisRow analysis);

    record AnalysisRow(
            UUID id, UUID vacancyId,
            String recommendation, int fitScore, int confidence,
            String summary, String[] reasonsToApply, String[] reasonsToSkip,
            String[] redFlags, String[] uncertainties, String[] missingInfo, String[] hardBlockers,
            Integer roleFit, Integer stackFit, Integer domainFit, Integer seniorityFit,
            Integer locationFit, Integer languageFit, Integer companyTypeFit,
            String germanRequirement, String relocationRisk, String salaryRisk, String freshnessRisk,
            String suggestedPositioning, String suggestedOutreachAngle, String suggestedSalaryStrategy,
            String suggestedFirstMessage, String suggestedNextAction, Integer suggestedPriority,
            String aiModel, Integer aiTokensUsed, Instant createdAt
    ) {}
}
