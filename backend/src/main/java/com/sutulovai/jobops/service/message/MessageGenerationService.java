package com.sutulovai.jobops.service.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sutulovai.jobops.config.OpenAiProperties;
import com.sutulovai.jobops.dto.request.GenerateMessageRequest;
import com.sutulovai.jobops.dto.request.LogCopiedMessageRequest;
import com.sutulovai.jobops.dto.response.OutreachMessageResponse;
import com.sutulovai.jobops.repository.*;
import com.sutulovai.jobops.service.ai.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MessageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(MessageGenerationService.class);

    private final OutreachMessageRepository messageRepository;
    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository applicationRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final OpenAiClient openAiClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    private final String systemPrompt;
    private final String userPromptTemplate;

    public MessageGenerationService(
            OutreachMessageRepository messageRepository,
            ContactRepository contactRepository,
            CompanyRepository companyRepository,
            VacancyRepository vacancyRepository,
            ApplicationRepository applicationRepository,
            JobAnalysisRepository jobAnalysisRepository,
            OpenAiClient openAiClient,
            OpenAiProperties openAiProperties,
            ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.vacancyRepository = vacancyRepository;
        this.applicationRepository = applicationRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.openAiClient = openAiClient;
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.systemPrompt = loadPrompt("prompts/message_generation_system.txt");
        this.userPromptTemplate = loadPrompt("prompts/message_generation_user.txt");
    }

    public OutreachMessageResponse generateMessage(UUID userId, GenerateMessageRequest req) {
        log.info("🔵 Generating {} message for user {}", req.messageType(), userId);

        // Gather context
        var contact = req.contactId() != null
                ? contactRepository.findById(req.contactId(), userId).orElse(null) : null;
        var company = req.companyId() != null
                ? companyRepository.findById(req.companyId(), userId).orElse(null) : null;
        var vacancy = req.vacancyId() != null
                ? vacancyRepository.findById(req.vacancyId(), userId).orElse(null) : null;
        var analysis = vacancy != null
                ? jobAnalysisRepository.findByVacancyId(vacancy.id()).orElse(null) : null;

        var hasApplied = req.applicationId() != null || (vacancy != null
                && applicationRepository.findByVacancyId(vacancy.id(), userId).isPresent());

        var applicationStage = "N/A";
        if (req.applicationId() != null) {
            applicationRepository.findById(req.applicationId(), userId)
                    .ifPresent(a -> {}); // just verify exists
        }

        // Build prompt
        var userPrompt = userPromptTemplate
                .replace("{messageType}", req.messageType())
                .replace("{recipientName}", contact != null ? contact.name() : "the recruiter")
                .replace("{recipientTitle}", contact != null ? orEmpty(contact.title()) : "Recruiter")
                .replace("{recipientType}", contact != null ? contact.contactType() : "RECRUITER")
                .replace("{companyName}", company != null ? company.name()
                        : (vacancy != null && vacancy.companyId() != null ? "[Company]" : "[Company]"))
                .replace("{roleTitle}", vacancy != null ? vacancy.title() : "[Role]")
                .replace("{fitScore}", analysis != null ? String.valueOf(analysis.fitScore()) : "N/A")
                .replace("{recommendation}", analysis != null ? analysis.recommendation() : "MAYBE")
                .replace("{priorityTier}", company != null ? company.priorityTier() : "P2")
                .replace("{hasApplied}", String.valueOf(hasApplied))
                .replace("{applicationStage}", applicationStage)
                .replace("{channel}", req.channel() != null ? req.channel() : "LINKEDIN")
                .replace("{relocationMention}", "yes")
                .replace("{tone}", req.tone() != null ? req.tone() : "professional")
                .replace("{customInstructions}", req.customInstructions() != null ? req.customInstructions() : "")
                .replace("{fitAngle}", analysis != null ? analysis.suggestedOutreachAngle() != null
                        ? analysis.suggestedOutreachAngle() : "" : "")
                .replace("{city}", company != null ? orEmpty(company.city()) : "Munich")
                .replace("{companyType}", company != null ? orEmpty(company.companyType()) : "PRODUCT");

        var result = openAiClient.complete(systemPrompt, userPrompt, openAiProperties.model(), 0.6, 800);

        JsonNode json;
        try {
            json = objectMapper.readTree(result.content());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse message generation response", e);
        }

        var msg = messageRepository.save(new OutreachMessageRepository.MessageRow(
                null, userId,
                req.contactId(), req.companyId(), req.vacancyId(), req.applicationId(),
                req.messageType(),
                req.channel() != null ? req.channel() : "LINKEDIN",
                contact != null ? contact.contactType() : "RECRUITER",
                json.path("generatedText").asText(""),
                null, "DRAFT",
                json.path("toneUsed").asText("professional"),
                1, null,
                null, null, null
        ));

        log.info("✅ Message generated: {}", msg.id());
        return toResponse(msg, contact != null ? contact.name() : null,
                company != null ? company.name() : null,
                vacancy != null ? vacancy.title() : null);
    }

    public List<OutreachMessageResponse> listMessages(UUID userId) {
        return messageRepository.findByUserId(userId)
                .stream().map(m -> toResponse(m, null, null, null)).toList();
    }

    public OutreachMessageResponse logCopiedMessage(UUID userId, LogCopiedMessageRequest req) {
        var contact = req.contactId() != null
                ? contactRepository.findById(req.contactId(), userId).orElse(null) : null;
        var company = req.companyId() != null
                ? companyRepository.findById(req.companyId(), userId).orElse(null) : null;
        var vacancy = req.vacancyId() != null
                ? vacancyRepository.findById(req.vacancyId(), userId).orElse(null) : null;

        var saved = messageRepository.save(new OutreachMessageRepository.MessageRow(
                null, userId,
                req.contactId(), req.companyId(), req.vacancyId(), req.applicationId(),
                req.messageType(),
                req.channel() != null ? req.channel() : "LINKEDIN",
                contact != null ? contact.contactType() : null,
                req.bodyText().trim(), null, "DRAFT",
                null, 1, null,
                null, null, null
        ));
        messageRepository.markCopied(saved.id(), userId);
        var refreshed = messageRepository.findById(saved.id(), userId).orElseThrow();
        return toResponse(refreshed, contact != null ? contact.name() : null,
                company != null ? company.name() : null,
                vacancy != null ? vacancy.title() : null);
    }

    public OutreachMessageResponse markCopied(UUID id, UUID userId) {
        var msg = messageRepository.findById(id, userId)
                .orElseThrow(() -> new com.sutulovai.jobops.exception.NotFoundException("Message not found: " + id));
        messageRepository.markCopied(id, userId);
        return toResponse(msg, null, null, null);
    }

    public OutreachMessageResponse markSent(UUID id, UUID userId) {
        var msg = messageRepository.findById(id, userId)
                .orElseThrow(() -> new com.sutulovai.jobops.exception.NotFoundException("Message not found: " + id));
        messageRepository.markSent(id, userId);
        return toResponse(msg, null, null, null);
    }

    private OutreachMessageResponse toResponse(OutreachMessageRepository.MessageRow m,
                                                String contactName, String companyName, String vacancyTitle) {
        return new OutreachMessageResponse(
                m.id(), m.userId(), m.contactId(), contactName,
                m.companyId(), companyName, m.vacancyId(), vacancyTitle,
                m.applicationId(), m.messageType(), m.channel(), m.recipientType(),
                m.generatedText(), m.editedFinalText(), m.status(), m.tone(),
                m.versionNumber(), m.nextActionId(),
                m.createdAt() != null ? m.createdAt() : Instant.now(),
                m.copiedAt(), m.sentAt()
        );
    }

    private static String orEmpty(String s) {
        return s != null ? s : "";
    }

    private static String loadPrompt(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load prompt: " + path, e);
        }
    }
}
