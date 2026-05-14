package com.sutulovai.jobops.dto.response;

import java.util.UUID;

public record AnalyzeJobResponse(
        UUID vacancyId,
        UUID companyId,
        VacancyResponse vacancy,
        JobAnalysisResponse analysis
) {
}
