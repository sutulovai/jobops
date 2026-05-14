package com.sutulovai.jobops.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String currentLocation,
        List<String> targetCountries,
        List<String> targetCities,
        List<String> backupCities,
        List<String> targetRoleTitles,
        Integer targetSalaryMin,
        Integer targetSalaryMax,
        Integer minimumSalary,
        Integer salaryStretchMax,
        String availability,
        String relocationStatus,
        String visaReadiness,
        String englishLevel,
        String germanLevel,
        List<String> preferredIndustries,
        List<String> rejectedIndustries,
        List<String> preferredCompanyTypes,
        List<String> rejectedCompanyTypes,
        String seniorityTarget,
        String positioningSummary,
        String outreachTone,
        String timezone,
        LocalDate searchStartDate
) {
}
