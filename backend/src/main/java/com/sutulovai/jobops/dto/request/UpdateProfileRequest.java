package com.sutulovai.jobops.dto.request;

import java.time.LocalDate;
import java.util.List;

public record UpdateProfileRequest(
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
