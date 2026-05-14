package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.AnalyzeJobRequest;
import com.sutulovai.jobops.dto.response.AnalyzeJobResponse;
import com.sutulovai.jobops.dto.response.JobAnalysisResponse;
import com.sutulovai.jobops.dto.response.VacancyResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.VacancyRepository;
import com.sutulovai.jobops.service.analysis.JobAnalysisService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vacancies")
@Tag(name = "Vacancies")
public class VacancyController {

    private final VacancyRepository vacancyRepository;
    private final JobAnalysisService jobAnalysisService;

    public VacancyController(VacancyRepository vacancyRepository, JobAnalysisService jobAnalysisService) {
        this.vacancyRepository = vacancyRepository;
        this.jobAnalysisService = jobAnalysisService;
    }

    @GetMapping
    public ResponseEntity<List<VacancyResponse>> list(
            @RequestParam(required = false) String status
    ) {
        var vacancies = vacancyRepository.findByUserId(SecurityUtils.currentUserId(), status);
        return ResponseEntity.ok(vacancies.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
                vacancyRepository.findById(id, SecurityUtils.currentUserId())
                        .map(this::toResponse)
                        .orElseThrow(() -> NotFoundException.forEntity("Vacancy", id))
        );
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeJobResponse> analyze(@Valid @RequestBody AnalyzeJobRequest request) {
        return ResponseEntity.ok(jobAnalysisService.analyze(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/{id}/re-evaluate")
    public ResponseEntity<AnalyzeJobResponse> reEvaluate(@PathVariable UUID id) {
        var userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(jobAnalysisService.reAnalyzeExistingVacancy(userId, id));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<JobAnalysisResponse> getAnalysis(@PathVariable UUID id) {
        return ResponseEntity.ok(jobAnalysisService.getAnalysis(id, SecurityUtils.currentUserId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest req) {
        vacancyRepository.updateStatus(id, SecurityUtils.currentUserId(), req.status());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vacancyRepository.findById(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> NotFoundException.forEntity("Vacancy", id));
        vacancyRepository.delete(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    private VacancyResponse toResponse(VacancyRepository.VacancyRow v) {
        return new VacancyResponse(
                v.id(), v.userId(), v.companyId(), null,
                v.title(), v.location(), v.remotePolicy(), v.url(), v.sourceChannel(),
                v.stackKeywords() != null ? Arrays.asList(v.stackKeywords()) : List.of(),
                v.domainKeywords() != null ? Arrays.asList(v.domainKeywords()) : List.of(),
                v.salaryRangeMin(), v.salaryRangeMax(), v.salaryCurrency(),
                v.languageRequirement(), v.relocationVisaWording(),
                v.seniority(), v.employmentType(), v.status(),
                v.aiFitScore(), v.aiConfidence(), v.aiRecommendation(), v.aiReasoning(),
                v.redFlags() != null ? Arrays.asList(v.redFlags()) : List.of(),
                v.uncertaintyFlags() != null ? Arrays.asList(v.uncertaintyFlags()) : List.of(),
                v.discoveredDate() != null ? v.discoveredDate().toString() : null,
                v.createdAt(), v.updatedAt()
        );
    }

    public record StatusUpdateRequest(String status) {}
}
