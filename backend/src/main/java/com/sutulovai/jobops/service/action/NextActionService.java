package com.sutulovai.jobops.service.action;

import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.ApplicationRepository;
import com.sutulovai.jobops.repository.NextActionRepository;
import com.sutulovai.jobops.repository.OutreachMessageRepository;
import com.sutulovai.jobops.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles next-action completion with cascade side effects on the linked application.
 */
@Service
public class NextActionService {

    private static final Logger log = LoggerFactory.getLogger(NextActionService.class);

    private final NextActionRepository nextActionRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final OutreachMessageRepository outreachMessageRepository;

    public NextActionService(NextActionRepository nextActionRepository,
                             ApplicationRepository applicationRepository,
                             ApplicationService applicationService,
                             OutreachMessageRepository outreachMessageRepository) {
        this.nextActionRepository = nextActionRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.outreachMessageRepository = outreachMessageRepository;
    }

    /**
     * Mark an action as done and apply cascade updates to the linked application.
     *
     * @param messageText optional body text (e.g. from client templates); when set, persists {@code outreach_messages} as sent.
     */
    @Transactional
    public void completeDone(UUID actionId, UUID userId, String messageText) {
        var action = nextActionRepository.findById(actionId, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Action", actionId));

        if ("APPLY_TO_JOB".equals(action.actionType())) {
            if (action.vacancyId() != null) {
                applicationService.completeApplyToJobAction(userId, action.vacancyId());
            }
            // Company-only apply reminders have no vacancy; applications require a vacancy FK — skip cascade.
        } else {
            var appId = action.applicationId();
            if (appId != null) {
                applyCascade(action.actionType(), appId, userId);
            }
        }

        if (messageText != null && !messageText.isBlank()) {
            var messageType = messageTypeForAction(action.actionType());
            if (messageType != null) {
                UUID appIdForMessage = action.applicationId();
                if (appIdForMessage == null && action.vacancyId() != null) {
                    appIdForMessage = applicationRepository.findByVacancyId(action.vacancyId(), userId)
                            .map(ApplicationRepository.ApplicationRow::id)
                            .orElse(null);
                }
                var saved = outreachMessageRepository.save(new OutreachMessageRepository.MessageRow(
                        null, userId,
                        action.contactId(), action.companyId(), action.vacancyId(), appIdForMessage,
                        messageType, "LINKEDIN", null,
                        messageText.trim(), null, "DRAFT",
                        null, 1, actionId,
                        null, null, null
                ));
                outreachMessageRepository.markSent(saved.id(), userId);
                log.info("📨 Saved outreach message {} for completed action {}", saved.id(), actionId);
            }
        }

        nextActionRepository.markDone(actionId, userId);
        log.info("✅ Action {} marked done (type={})", actionId, action.actionType());
    }

    private static String messageTypeForAction(String actionType) {
        return switch (actionType) {
            case "APPLY_TO_JOB", "CONTACT_RECRUITER" -> "LINKEDIN_RECRUITER_DM";
            case "CONTACT_HIRING_MANAGER" -> "LINKEDIN_MANAGER_DM";
            case "REQUEST_REFERRAL" -> "REFERRAL_REQUEST";
            case "FOLLOW_UP_RECRUITER", "FOLLOW_UP_MANAGER", "FOLLOW_UP_REFERRAL_CONTACT" -> "FOLLOW_UP";
            case "SEND_POST_INTERVIEW_THANK_YOU" -> "POST_INTERVIEW_THANK_YOU";
            default -> null;
        };
    }

    private void applyCascade(String actionType, UUID appId, UUID userId) {
        switch (actionType) {
            case "CONTACT_RECRUITER" -> applicationRepository.setRecruiterContacted(appId, userId);
            case "CONTACT_HIRING_MANAGER" -> applicationRepository.setHiringManagerContacted(appId, userId);
            case "REQUEST_REFERRAL" -> applicationRepository.setReferralRequested(appId, userId);
            case "FOLLOW_UP_RECRUITER", "FOLLOW_UP_MANAGER", "FOLLOW_UP_REFERRAL_CONTACT" ->
                applicationRepository.incrementFollowUp(appId, userId);
            case "SEND_POST_INTERVIEW_THANK_YOU" -> applicationRepository.updateLastContactDate(appId, userId);
            default -> log.debug("No cascade for action type: {}", actionType);
        }
    }
}
