package com.sutulovai.jobops.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyzeJobRequest(
        @NotBlank @Size(min = 100, message = "Job description must be at least 100 characters")
        String jobDescription,
        String jobUrl,
        String companyName,
        String roleTitle,
        String location,
        String sourceChannel,
        String sourceNotes,
        String salaryInfo,
        String languageRequirement,
        String relocationWording,
        String recruiterInfo,
        String personalNote
) {
}
