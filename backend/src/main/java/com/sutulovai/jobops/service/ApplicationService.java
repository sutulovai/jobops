package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.request.CreateApplicationRequest;
import com.sutulovai.jobops.dto.response.ApplicationResponse;
import com.sutulovai.jobops.exception.ConflictException;
import com.sutulovai.jobops.exception.ErrorCode;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.ApplicationRepository;
import com.sutulovai.jobops.repository.CompanyRepository;
import com.sutulovai.jobops.repository.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private static final Map<String, List<String>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = new java.util.HashMap<>();
        VALID_TRANSITIONS.put("ADDED_TO_PIPELINE", List.of("APPLIED", "ARCHIVED"));
        VALID_TRANSITIONS.put("APPLIED", List.of("RECRUITER_CONTACTED", "HIRING_MANAGER_CONTACTED",
                "REFERRAL_REQUESTED", "RECRUITER_SCREEN_SCHEDULED", "REJECTED", "GHOSTED", "WITHDRAWN"));
        VALID_TRANSITIONS.put("RECRUITER_CONTACTED", List.of("RECRUITER_SCREEN_SCHEDULED", "REJECTED", "GHOSTED", "WITHDRAWN"));
        VALID_TRANSITIONS.put("REFERRAL_REQUESTED", List.of("RECRUITER_SCREEN_SCHEDULED", "REJECTED", "GHOSTED"));
        VALID_TRANSITIONS.put("HIRING_MANAGER_CONTACTED", List.of("RECRUITER_SCREEN_SCHEDULED", "REJECTED", "GHOSTED"));
        VALID_TRANSITIONS.put("RECRUITER_SCREEN_SCHEDULED", List.of("RECRUITER_SCREEN_DONE", "REJECTED", "GHOSTED"));
        VALID_TRANSITIONS.put("RECRUITER_SCREEN_DONE", List.of("TECHNICAL_INTERVIEW_SCHEDULED", "FINAL_INTERVIEW", "OFFER", "REJECTED"));
        VALID_TRANSITIONS.put("TECHNICAL_INTERVIEW_SCHEDULED", List.of("TECHNICAL_INTERVIEW_DONE", "REJECTED", "GHOSTED"));
        VALID_TRANSITIONS.put("TECHNICAL_INTERVIEW_DONE", List.of("FINAL_INTERVIEW", "OFFER", "REJECTED"));
        VALID_TRANSITIONS.put("FINAL_INTERVIEW", List.of("OFFER", "REJECTED"));
        VALID_TRANSITIONS.put("OFFER", List.of("WITHDRAWN"));
        VALID_TRANSITIONS.put("REJECTED", List.of("ARCHIVED"));
        VALID_TRANSITIONS.put("GHOSTED", List.of("ARCHIVED"));
    }

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                               VacancyRepository vacancyRepository,
                               CompanyRepository companyRepository) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.companyRepository = companyRepository;
    }

    public List<ApplicationResponse> listApplications(UUID userId) {
        return applicationRepository.findByUserId(userId).stream()
                .map(a -> toResponse(a, userId))
                .toList();
    }

    public ApplicationResponse getApplication(UUID id, UUID userId) {
        return applicationRepository.findById(id, userId)
                .map(a -> toResponse(a, userId))
                .orElseThrow(() -> NotFoundException.forEntity("Application", id));
    }

    /**
     * Mark an "apply to job" next-action as done: ensure an application exists for the vacancy,
     * move it to APPLIED with {@code date_applied} set (Berlin "today"), and align vacancy status.
     */
    @Transactional
    public void completeApplyToJobAction(UUID userId, UUID vacancyId) {
        var vacancy = vacancyRepository.findById(vacancyId, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Vacancy", vacancyId));
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));
        var cityCategory = detectCityCategory(vacancy.location());

        var existing = applicationRepository.findByVacancyId(vacancyId, userId);
        if (existing.isEmpty()) {
            applicationRepository.save(new ApplicationRepository.ApplicationRow(
                    null, userId, vacancyId, vacancy.companyId(), null,
                    "APPLIED",
                    null, vacancy.sourceChannel(),
                    today, false, false, false,
                    0, null, today.plusDays(1),
                    50, false, null,
                    null, null, cityCategory,
                    null, null
            ));
            vacancyRepository.updateStatus(vacancyId, userId, "APPLIED");
            log.info("✅ Apply action: created application APPLIED for vacancy {}", vacancyId);
            return;
        }

        var app = existing.get();
        if ("ADDED_TO_PIPELINE".equals(app.stage())) {
            applicationRepository.updateStage(app.id(), userId, "APPLIED");
            var updated = new ApplicationRepository.ApplicationRow(
                    app.id(), userId, app.vacancyId(), app.companyId(), app.cvId(),
                    "APPLIED", app.applicationChannel(), app.sourceChannel(),
                    today, app.recruiterContacted(),
                    app.hiringManagerContacted(), app.referralRequested(),
                    app.followUpCount(), app.lastContactDate(), app.nextActionDate(),
                    app.priority(), app.stale(), app.notes(), app.outcome(),
                    app.rejectionReason(), app.cityCategory(), app.createdAt(), null
            );
            applicationRepository.save(updated);
            vacancyRepository.updateStatus(vacancyId, userId, "APPLIED");
            log.info("✅ Apply action: advanced application {} to APPLIED", app.id());
        } else if ("APPLIED".equals(app.stage()) && app.dateApplied() == null) {
            var updated = new ApplicationRepository.ApplicationRow(
                    app.id(), userId, app.vacancyId(), app.companyId(), app.cvId(),
                    "APPLIED", app.applicationChannel(), app.sourceChannel(),
                    today, app.recruiterContacted(),
                    app.hiringManagerContacted(), app.referralRequested(),
                    app.followUpCount(), app.lastContactDate(), app.nextActionDate(),
                    app.priority(), app.stale(), app.notes(), app.outcome(),
                    app.rejectionReason(), app.cityCategory(), app.createdAt(), null
            );
            applicationRepository.save(updated);
            vacancyRepository.updateStatus(vacancyId, userId, "APPLIED");
            log.info("✅ Apply action: set date_applied for application {}", app.id());
        }
    }

    @Transactional
    public ApplicationResponse addToPipeline(UUID userId, CreateApplicationRequest req) {
        var vacancy = vacancyRepository.findById(req.vacancyId(), userId)
                .orElseThrow(() -> NotFoundException.forEntity("Vacancy", req.vacancyId()));

        if (applicationRepository.findByVacancyId(req.vacancyId(), userId).isPresent()) {
            throw new ConflictException(ErrorCode.CONFLICT, "Application for this vacancy already exists");
        }

        // Determine city category from vacancy location
        var cityCategory = req.cityCategory() != null ? req.cityCategory()
                : detectCityCategory(vacancy.location());

        var app = applicationRepository.save(new ApplicationRepository.ApplicationRow(
                null, userId, req.vacancyId(), vacancy.companyId(), req.cvId(),
                "ADDED_TO_PIPELINE",
                req.applicationChannel(), vacancy.sourceChannel(),
                null, false, false, false,
                0, null, LocalDate.now().plusDays(1),
                50, false, req.notes(),
                null, null, cityCategory,
                null, null
        ));

        // Update vacancy status
        vacancyRepository.updateStatus(req.vacancyId(), userId, "ADDED_TO_PIPELINE");

        log.info("✅ Added to pipeline: {}", app.id());
        return toResponse(app, userId);
    }

    public ApplicationResponse advanceStage(UUID id, UUID userId, String newStage) {
        var app = applicationRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Application", id));

        var validNext = VALID_TRANSITIONS.getOrDefault(app.stage(), List.of());
        if (!validNext.contains(newStage)) {
            throw new ConflictException(ErrorCode.INVALID_STAGE_TRANSITION,
                    "Cannot transition from " + app.stage() + " to " + newStage);
        }

        applicationRepository.updateStage(id, userId, newStage);

        // Side effects for specific transitions
        if ("APPLIED".equals(newStage)) {
            var updated = new ApplicationRepository.ApplicationRow(
                    app.id(), userId, app.vacancyId(), app.companyId(), app.cvId(),
                    newStage, app.applicationChannel(), app.sourceChannel(),
                    LocalDate.now(), app.recruiterContacted(),
                    app.hiringManagerContacted(), app.referralRequested(),
                    app.followUpCount(), app.lastContactDate(), app.nextActionDate(),
                    app.priority(), app.stale(), app.notes(), app.outcome(),
                    app.rejectionReason(), app.cityCategory(), app.createdAt(), null
            );
            applicationRepository.save(updated);
            vacancyRepository.updateStatus(app.vacancyId(), userId, "APPLIED");
        }

        return applicationRepository.findById(id, userId)
                .map(a -> toResponse(a, userId))
                .orElseThrow();
    }

    public void recordFollowUp(UUID id, UUID userId) {
        applicationRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Application", id));
        applicationRepository.incrementFollowUp(id, userId);
    }

    public ApplicationResponse updateNotes(UUID id, UUID userId, String notes) {
        var app = applicationRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Application", id));
        var updated = new ApplicationRepository.ApplicationRow(
                app.id(), userId, app.vacancyId(), app.companyId(), app.cvId(),
                app.stage(), app.applicationChannel(), app.sourceChannel(),
                app.dateApplied(), app.recruiterContacted(), app.hiringManagerContacted(),
                app.referralRequested(), app.followUpCount(), app.lastContactDate(),
                app.nextActionDate(), app.priority(), app.stale(), notes,
                app.outcome(), app.rejectionReason(), app.cityCategory(), null, null
        );
        return toResponse(applicationRepository.save(updated), userId);
    }

    private ApplicationResponse toResponse(ApplicationRepository.ApplicationRow a, UUID userId) {
        String vacancyTitle = null;
        String companyName = null;
        try {
            var vac = vacancyRepository.findById(a.vacancyId(), userId).orElse(null);
            if (vac != null) {
                vacancyTitle = vac.title();
                if (vac.companyId() != null) {
                    companyName = companyRepository.findById(vac.companyId(), userId)
                            .map(c -> c.name())
                            .orElse(null);
                }
            }
        } catch (Exception ignored) {}

        return new ApplicationResponse(
                a.id(), a.userId(), a.vacancyId(), vacancyTitle,
                a.companyId(), companyName,
                a.cvId(), a.stage(), a.applicationChannel(), a.sourceChannel(),
                a.dateApplied() != null ? a.dateApplied().toString() : null,
                a.recruiterContacted(), a.hiringManagerContacted(),
                a.referralRequested(), a.followUpCount(),
                a.lastContactDate() != null ? a.lastContactDate().toString() : null,
                a.nextActionDate() != null ? a.nextActionDate().toString() : null,
                a.priority(), a.stale(), a.notes(),
                a.outcome(), a.rejectionReason(), a.cityCategory(),
                a.createdAt(), a.updatedAt()
        );
    }

    private static String detectCityCategory(String location) {
        if (location == null) return null;
        var l = location.toLowerCase();
        if (l.contains("munich") || l.contains("münchen")) return "MUNICH";
        if (l.contains("berlin")) return "BERLIN";
        if (l.contains("hamburg")) return "HAMBURG";
        if (l.contains("frankfurt")) return "FRANKFURT";
        if (l.contains("stuttgart")) return "STUTTGART";
        if (l.contains("nuremberg") || l.contains("nürnberg")) return "NUREMBERG";
        if (l.contains("remote")) return "REMOTE";
        return "OTHER";
    }
}
