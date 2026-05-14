package com.sutulovai.jobops.service.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sutulovai.jobops.config.OpenAiProperties;
import com.sutulovai.jobops.dto.request.AnalyzeJobRequest;
import com.sutulovai.jobops.dto.response.AnalyzeJobResponse;
import com.sutulovai.jobops.dto.response.JobAnalysisResponse;
import com.sutulovai.jobops.dto.response.VacancyResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.CompanyRepository;
import com.sutulovai.jobops.repository.JobAnalysisRepository;
import com.sutulovai.jobops.repository.ProfileRepository;
import com.sutulovai.jobops.repository.VacancyRepository;
import com.sutulovai.jobops.service.ai.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class JobAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(JobAnalysisService.class);

    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final ProfileRepository profileRepository;
    private final OpenAiClient openAiClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    private final String systemPrompt;
    private final String userPromptTemplate;

    public JobAnalysisService(
            VacancyRepository vacancyRepository,
            CompanyRepository companyRepository,
            JobAnalysisRepository jobAnalysisRepository,
            ProfileRepository profileRepository,
            OpenAiClient openAiClient,
            OpenAiProperties openAiProperties,
            ObjectMapper objectMapper
    ) {
        this.vacancyRepository = vacancyRepository;
        this.companyRepository = companyRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.profileRepository = profileRepository;
        this.openAiClient = openAiClient;
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.systemPrompt = loadPrompt("prompts/jd_analysis_system.txt");
        this.userPromptTemplate = loadPrompt("prompts/jd_analysis_user.txt");
    }

    @Transactional
    public AnalyzeJobResponse analyze(UUID userId, AnalyzeJobRequest request) {
        return analyzeInternal(userId, request, null);
    }

    /**
     * Re-run JD analysis for an existing vacancy; updates the same row and upserts {@code job_analyses}.
     */
    @Transactional
    public AnalyzeJobResponse reAnalyzeExistingVacancy(UUID userId, UUID vacancyId) {
        var existing = vacancyRepository.findById(vacancyId, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Vacancy", vacancyId));
        String companyName = null;
        if (existing.companyId() != null) {
            companyName = companyRepository.findById(existing.companyId(), userId)
                    .map(CompanyRepository.CompanyRow::name)
                    .orElse(null);
        }
        var request = new AnalyzeJobRequest(
                existing.jobDescriptionText(),
                existing.url(),
                companyName,
                existing.title(),
                existing.location(),
                existing.sourceChannel(),
                null, null,
                existing.languageRequirement(),
                existing.relocationVisaWording(),
                null, null
        );
        return analyzeInternal(userId, request, vacancyId);
    }

    private AnalyzeJobResponse analyzeInternal(UUID userId, AnalyzeJobRequest request, UUID existingVacancyId) {
        log.info("🔵 Analyzing JD for user {} (existingVacancyId={})", userId, existingVacancyId);

        // Find or create company
        UUID companyId = null;
        String companyData = "No stored data.";
        if (request.companyName() != null && !request.companyName().isBlank()) {
            var existingCompany = companyRepository.findByName(request.companyName(), userId);
            if (existingCompany.isPresent()) {
                companyId = existingCompany.get().id();
                companyData = String.format(
                        "Priority tier: %s, English-speaking: %s, Relocation: %s, Salary pitch: €%s–€%s, Fit reason: %s",
                        existingCompany.get().priorityTier(),
                        existingCompany.get().englishLikelihood(),
                        existingCompany.get().relocationFriendly(),
                        existingCompany.get().salaryPitchMin(),
                        existingCompany.get().salaryPitchMax(),
                        existingCompany.get().fitReason()
                );
            }
        }

        // Calculate search week
        int searchWeek = 1;
        var todayBerlin = LocalDate.now(ZoneId.of("Europe/Berlin"));
        var profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null && profile.searchStartDate() != null) {
            long days = todayBerlin.toEpochDay() - profile.searchStartDate().toEpochDay();
            searchWeek = Math.max(1, (int) (days / 7) + 1);
        }

        // Build user prompt
        var userPrompt = userPromptTemplate
                .replace("{jobDescription}", request.jobDescription())
                .replace("{companyName}", orEmpty(request.companyName()))
                .replace("{roleTitle}", orEmpty(request.roleTitle()))
                .replace("{location}", orEmpty(request.location()))
                .replace("{sourceChannel}", orEmpty(request.sourceChannel()))
                .replace("{salaryInfo}", orEmpty(request.salaryInfo()))
                .replace("{languageRequirement}", orEmpty(request.languageRequirement()))
                .replace("{relocationWording}", orEmpty(request.relocationWording()))
                .replace("{personalNote}", orEmpty(request.personalNote()))
                .replace("{companyData}", companyData)
                .replace("{searchWeek}", String.valueOf(searchWeek));

        // Call OpenAI
        var result = openAiClient.complete(
                systemPrompt, userPrompt,
                openAiProperties.analysisModel(),
                0.3, 2000
        );

        // Parse response
        JsonNode json;
        try {
            json = objectMapper.readTree(result.content());
        } catch (IOException e) {
            log.error("❌ Failed to parse OpenAI response: {}", result.content());
            throw new RuntimeException("Failed to parse AI analysis response", e);
        }

        // Use AI-extracted fields as fallbacks when user didn't provide them
        String resolvedTitle = orEmpty(request.roleTitle(),
                json.path("extractedTitle").asText(null) != null
                        ? json.path("extractedTitle").asText() : "Unknown Role");
        String resolvedLocation = orEmpty(request.location(),
                json.path("extractedLocation").asText(null));
        String resolvedLanguage = orEmpty(request.languageRequirement(),
                json.path("extractedLanguageRequirement").asText(null));
        String resolvedRelocation = orEmpty(request.relocationWording(),
                json.path("extractedRelocationWording").asText(null));

        // Also try to resolve company from extracted name if not already found
        String resolvedCompanyName = request.companyName();
        if ((resolvedCompanyName == null || resolvedCompanyName.isBlank())
                && !json.path("extractedCompany").isMissingNode()
                && !json.path("extractedCompany").isNull()) {
            resolvedCompanyName = json.path("extractedCompany").asText();
        }
        if (companyId == null && resolvedCompanyName != null && !resolvedCompanyName.isBlank()) {
            var found = companyRepository.findByName(resolvedCompanyName, userId);
            if (found.isPresent()) {
                companyId = found.get().id();
            }
        }

        var rec = json.path("recommendation").asText("MAYBE");
        var newStatus = switch (rec) {
            case "APPLY" -> "SHOULD_APPLY";
            case "SKIP" -> "SKIP";
            default -> "MAYBE";
        };

        VacancyRepository.VacancyRow vacancy;
        if (existingVacancyId != null) {
            var existing = vacancyRepository.findById(existingVacancyId, userId)
                    .orElseThrow(() -> NotFoundException.forEntity("Vacancy", existingVacancyId));
            UUID cid = companyId != null ? companyId : existing.companyId();
            String vacancyStatus = preserveVacancyStatusForReanalysis(existing.status())
                    ? existing.status()
                    : newStatus;
            vacancy = vacancyRepository.save(new VacancyRepository.VacancyRow(
                    existingVacancyId,
                    userId,
                    cid,
                    resolvedTitle,
                    resolvedLocation,
                    existing.remotePolicy(),
                    orEmpty(request.jobUrl(), existing.url()),
                    orEmpty(request.sourceChannel(), existing.sourceChannel()),
                    request.jobDescription(),
                    toStringArray(json.path("stackKeywords")),
                    toStringArray(json.path("domainKeywords")),
                    existing.salaryRangeMin(),
                    existing.salaryRangeMax(),
                    existing.salaryCurrency() != null ? existing.salaryCurrency() : "EUR",
                    resolvedLanguage,
                    resolvedRelocation,
                    existing.seniority(),
                    existing.employmentType(),
                    vacancyStatus,
                    json.path("fitScore").asInt(0),
                    json.path("confidence").asInt(0),
                    json.path("recommendation").asText("MAYBE"),
                    json.path("summary").asText(""),
                    toStringArray(json.path("redFlags")),
                    toStringArray(json.path("uncertainties")),
                    existing.discoveredDate() != null ? existing.discoveredDate() : todayBerlin,
                    existing.createdAt(),
                    null
            ));
        } else {
            vacancy = vacancyRepository.save(new VacancyRepository.VacancyRow(
                    null, userId, companyId,
                    resolvedTitle,
                    resolvedLocation, null,
                    request.jobUrl(), request.sourceChannel(),
                    request.jobDescription(),
                    toStringArray(json.path("stackKeywords")),
                    toStringArray(json.path("domainKeywords")),
                    null, null, "EUR",
                    resolvedLanguage, resolvedRelocation,
                    null, null,
                    "ANALYZING",
                    json.path("fitScore").asInt(0),
                    json.path("confidence").asInt(0),
                    json.path("recommendation").asText("MAYBE"),
                    json.path("summary").asText(""),
                    toStringArray(json.path("redFlags")),
                    toStringArray(json.path("uncertainties")),
                    todayBerlin, null, null
            ));
            vacancyRepository.updateStatus(vacancy.id(), userId, newStatus);
            vacancy = vacancyRepository.findById(vacancy.id(), userId).orElseThrow();
        }

        var analysis = jobAnalysisRepository.save(new JobAnalysisRepository.AnalysisRow(
                null, vacancy.id(),
                json.path("recommendation").asText("MAYBE"),
                json.path("fitScore").asInt(0),
                json.path("confidence").asInt(70),
                json.path("summary").asText(""),
                toStringArray(json.path("reasonsToApply")),
                toStringArray(json.path("reasonsToSkip")),
                toStringArray(json.path("redFlags")),
                toStringArray(json.path("uncertainties")),
                toStringArray(json.path("missingInfo")),
                toStringArray(json.path("hardBlockers")),
                json.path("roleFit").asInt(50),
                json.path("stackFit").asInt(50),
                json.path("domainFit").asInt(50),
                json.path("seniorityFit").asInt(50),
                json.path("locationFit").asInt(50),
                json.path("languageFit").asInt(50),
                json.path("companyTypeFit").asInt(50),
                json.path("germanRequirement").asText("UNKNOWN"),
                json.path("relocationRisk").asText("UNCERTAIN"),
                json.path("salaryRisk").asText("UNCERTAIN"),
                json.path("freshnessRisk").asText("UNKNOWN"),
                json.path("suggestedPositioning").asText(null),
                json.path("suggestedOutreachAngle").asText(null),
                json.path("suggestedSalaryStrategy").asText(null),
                json.path("suggestedFirstMessage").asText(null),
                json.path("suggestedNextAction").asText(null),
                json.path("suggestedPriority").asInt(50),
                result.model(),
                result.promptTokens() + result.completionTokens(),
                null
        ));

        final String finalCompanyName = resolvedCompanyName;
        log.info("✅ Analysis complete: {} with score {}", rec, json.path("fitScore").asInt(0));
        return new AnalyzeJobResponse(
                vacancy.id(), companyId,
                toVacancyResponse(vacancy, finalCompanyName),
                toAnalysisResponse(analysis)
        );
    }

    public JobAnalysisResponse getAnalysis(UUID vacancyId, UUID userId) {
        vacancyRepository.findById(vacancyId, userId)
                .orElseThrow(() -> new com.sutulovai.jobops.exception.NotFoundException("Vacancy not found: " + vacancyId));
        return jobAnalysisRepository.findByVacancyId(vacancyId)
                .map(this::toAnalysisResponse)
                .orElseThrow(() -> new com.sutulovai.jobops.exception.NotFoundException("Analysis not found for vacancy: " + vacancyId));
    }

    private VacancyResponse toVacancyResponse(VacancyRepository.VacancyRow v, String companyName) {
        return new VacancyResponse(
                v.id(), v.userId(), v.companyId(), companyName,
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

    private JobAnalysisResponse toAnalysisResponse(JobAnalysisRepository.AnalysisRow a) {
        return new JobAnalysisResponse(
                a.id(), a.vacancyId(),
                a.recommendation(), a.fitScore(), a.confidence(), a.summary(),
                a.reasonsToApply() != null ? Arrays.asList(a.reasonsToApply()) : List.of(),
                a.reasonsToSkip() != null ? Arrays.asList(a.reasonsToSkip()) : List.of(),
                a.redFlags() != null ? Arrays.asList(a.redFlags()) : List.of(),
                a.uncertainties() != null ? Arrays.asList(a.uncertainties()) : List.of(),
                a.missingInfo() != null ? Arrays.asList(a.missingInfo()) : List.of(),
                a.hardBlockers() != null ? Arrays.asList(a.hardBlockers()) : List.of(),
                a.roleFit(), a.stackFit(), a.domainFit(), a.seniorityFit(),
                a.locationFit(), a.languageFit(), a.companyTypeFit(),
                a.germanRequirement(), a.relocationRisk(), a.salaryRisk(), a.freshnessRisk(),
                a.suggestedPositioning(), a.suggestedOutreachAngle(), a.suggestedSalaryStrategy(),
                a.suggestedFirstMessage(), a.suggestedNextAction(), a.suggestedPriority(),
                a.createdAt()
        );
    }

    private static String[] toStringArray(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isArray()) return new String[0];
        var result = new String[node.size()];
        for (int i = 0; i < node.size(); i++) {
            result[i] = node.get(i).asText();
        }
        return result;
    }

    private static String orEmpty(String s) {
        return s != null ? s : "";
    }

    private static String orEmpty(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private static boolean preserveVacancyStatusForReanalysis(String status) {
        if (status == null) {
            return false;
        }
        return !Set.of("SHOULD_APPLY", "MAYBE", "SKIP", "ANALYZING", "DISCOVERED").contains(status);
    }

    private static String loadPrompt(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load prompt: " + path, e);
        }
    }
}
