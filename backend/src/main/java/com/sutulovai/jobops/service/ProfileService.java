package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.request.UpdateProfileRequest;
import com.sutulovai.jobops.dto.response.ProfileResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileResponse getProfile(UUID userId) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundException.forEntity("Profile", userId));
        return toResponse(profile);
    }

    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        log.info("🔵 Updating profile for user {}", userId);
        var existing = profileRepository.findByUserId(userId).orElse(null);
        var updated = new ProfileRepository.ProfileRow(
                existing != null ? existing.id() : null,
                userId,
                req.fullName(),
                req.currentLocation(),
                orEmpty(req.targetCountries()),
                orEmpty(req.targetCities()),
                orEmpty(req.backupCities()),
                orEmpty(req.targetRoleTitles()),
                req.targetSalaryMin(),
                req.targetSalaryMax(),
                req.minimumSalary(),
                req.salaryStretchMax(),
                req.availability(),
                req.relocationStatus(),
                req.visaReadiness(),
                req.englishLevel(),
                req.germanLevel(),
                orEmpty(req.preferredIndustries()),
                orEmpty(req.rejectedIndustries()),
                orEmpty(req.preferredCompanyTypes()),
                orEmpty(req.rejectedCompanyTypes()),
                req.seniorityTarget(),
                req.positioningSummary(),
                req.outreachTone(),
                req.timezone() != null ? req.timezone() : "Europe/Berlin",
                req.searchStartDate()
        );
        var saved = profileRepository.upsert(updated);
        log.info("✅ Profile updated for user {}", userId);
        return toResponse(saved);
    }

    private ProfileResponse toResponse(ProfileRepository.ProfileRow p) {
        return new ProfileResponse(
                p.id(), p.userId(), p.fullName(), p.currentLocation(),
                p.targetCountries(), p.targetCities(), p.backupCities(), p.targetRoleTitles(),
                p.targetSalaryMin(), p.targetSalaryMax(), p.minimumSalary(), p.salaryStretchMax(),
                p.availability(), p.relocationStatus(), p.visaReadiness(), p.englishLevel(),
                p.germanLevel(), p.preferredIndustries(), p.rejectedIndustries(),
                p.preferredCompanyTypes(), p.rejectedCompanyTypes(), p.seniorityTarget(),
                p.positioningSummary(), p.outreachTone(), p.timezone(), p.searchStartDate()
        );
    }

    private List<String> orEmpty(List<String> list) {
        return list != null ? list : List.of();
    }
}
