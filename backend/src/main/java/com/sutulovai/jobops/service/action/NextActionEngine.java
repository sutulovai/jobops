package com.sutulovai.jobops.service.action;

import com.sutulovai.jobops.repository.*;
import com.sutulovai.jobops.util.DbTime;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

/**
 * Deterministic next-action engine.
 *
 * Runs on schedule and in response to user events to generate, prioritise, and deduplicate
 * next actions based on Artem's Germany job-search strategy:
 *
 * Priority tiers:
 *   P0 (score 100-90): Overdue critical actions (follow-up deadline passed, interview today, etc.)
 *   P1 (score 89-70):  Apply to high-fit jobs, contact recruiter for P1 companies
 *   P2 (score 69-50):  Standard follow-ups, saved search checks
 *   P3 (score 49-0):   Administrative, batch actions
 *
 * Weekly targets:
 *   Week 1: 15-18 applications, 12 recruiter messages, 8 EM messages, 10 referral messages
 *   Week 2+: 18-20 applications, 10 recruiter, 10 EM, 10-12 referral, expand cities
 *   Week 3+: Funnel retrospective, selective Stuttgart/Nuremberg/remote
 *   Week 4+: Follow-ups dominant, re-prioritize on response rates
 */
@Service
public class NextActionEngine {

    private static final Logger log = LoggerFactory.getLogger(NextActionEngine.class);

    private final NextActionRepository nextActionRepository;
    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final ProfileRepository profileRepository;
    private final DSLContext dsl;

    public NextActionEngine(
            NextActionRepository nextActionRepository,
            ApplicationRepository applicationRepository,
            VacancyRepository vacancyRepository,
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            ProfileRepository profileRepository,
            DSLContext dsl
    ) {
        this.nextActionRepository = nextActionRepository;
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.profileRepository = profileRepository;
        this.dsl = dsl;
    }

    /**
     * Runs every weekday at 7 AM Europe/Berlin to recalculate pending actions.
     */
    @Scheduled(cron = "0 0 7 * * MON-FRI", zone = "Europe/Berlin")
    public void scheduledRecalculation() {
        log.info("⏰ Scheduled next-action recalculation triggered");
        recalculateForAllUsers();
    }

    /**
     * Recalculate for all users.
     */
    public void recalculateForAllUsers() {
        var userIds = dsl.select(field("id", UUID.class))
                .from(table("users"))
                .fetch(field("id", UUID.class));
        for (var userId : userIds) {
            try {
                recalculateForUser(userId);
            } catch (Exception e) {
                log.error("❌ Failed to recalculate actions for user {}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * Full recalculation for a single user. Safe to call multiple times (idempotent via dedup).
     */
    @Transactional
    public void recalculateForUser(UUID userId) {
        log.info("🔵 Recalculating next actions for user {}", userId);
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        generateApplyReminders(userId, today);
        generateFollowUpReminders(userId, today);
        generateReferralReminders(userId, today);
        generateSavedSearchReminders(userId, today);
        generateWeeklyTargetReminders(userId, today);
        generateDecisionReminders(userId, today);
        generateRejectionAnalysisReminders(userId, today);

        log.info("✅ Next action recalculation complete for user {}", userId);
    }

    /**
     * Generate "Apply now" actions for ACTIVE_TARGET or WATCHLIST companies with no application yet.
     * Prefers a SHOULD_APPLY/MAYBE vacancy when present; otherwise surfaces a company-scoped apply reminder.
     */
    private void generateApplyReminders(UUID userId, LocalDate today) {
        var targets = companyRepository.findByUserId(userId).stream()
                .filter(c -> "ACTIVE_TARGET".equals(c.status()) || "WATCHLIST".equals(c.status()))
                .toList();

        // Collect company IDs that already have an application
        var appliedCompanyIds = applicationRepository.findByUserId(userId).stream()
                .filter(a -> a.companyId() != null)
                .map(ApplicationRepository.ApplicationRow::companyId)
                .collect(java.util.stream.Collectors.toSet());

        for (var company : targets) {
            if (appliedCompanyIds.contains(company.id())) continue;

            // Find a SHOULD_APPLY vacancy for this company
            var vacancies = vacancyRepository.findByUserId(userId, null).stream()
                    .filter(v -> company.id().equals(v.companyId())
                            && ("SHOULD_APPLY".equals(v.status()) || "MAYBE".equals(v.status())))
                    .toList();

            int score = "P1".equals(company.priorityTier()) ? 90 : "P1_5".equals(company.priorityTier()) ? 85 : 75;
            if (!vacancies.isEmpty()) {
                var vacancy = vacancies.get(0);
                createActionIfNotExists(userId, "APPLY_TO_JOB", "P1", score,
                        today.toString(),
                        company.name() + ": " + vacancy.title() + " — target company with no application yet",
                        company.id(), vacancy.id(), null, null, null, null,
                        true, "LINKEDIN_RECRUITER_DM");
            } else {
                createActionIfNotExists(userId, "APPLY_TO_JOB", "P1", score,
                        today.toString(),
                        company.name() + ": apply via careers — active target with no analyzed vacancy yet",
                        company.id(), null, null, null, null, null,
                        true, "LINKEDIN_RECRUITER_DM");
            }
        }
    }

    /**
     * Generate referral request reminders for warm contacts at companies with open vacancies.
     */
    private void generateReferralReminders(UUID userId, LocalDate today) {
        var contacts = contactRepository.findByUserId(userId).stream()
                .filter(c -> "WARM".equalsIgnoreCase(c.status()))
                .filter(c -> relationshipStrengthAtLeast(c.relationshipStrength(), 3))
                .toList();

        // Collect company IDs that already have a referral requested
        var referralDoneCompanyIds = applicationRepository.findByUserId(userId).stream()
                .filter(a -> a.referralRequested() && a.companyId() != null)
                .map(ApplicationRepository.ApplicationRow::companyId)
                .collect(java.util.stream.Collectors.toSet());

        for (var contact : contacts) {
            if (contact.companyId() == null) continue;
            if (referralDoneCompanyIds.contains(contact.companyId())) continue;

            var daysSince = daysSinceLastContact(contact.lastContactedDate(), today);
            if (daysSince == null || daysSince <= 14) continue;

            // Check if there's an open vacancy at their company
            var hasOpenVacancy = vacancyRepository.findByUserId(userId, null).stream()
                    .anyMatch(v -> contact.companyId().equals(v.companyId())
                            && ("SHOULD_APPLY".equals(v.status()) || "MAYBE".equals(v.status())
                            || "ADDED_TO_PIPELINE".equals(v.status()) || "APPLIED".equals(v.status())));

            if (hasOpenVacancy) {
                createActionIfNotExists(userId, "REQUEST_REFERRAL", "P2", 65,
                        today.toString(),
                        contact.name() + " — warm contact, no outreach in 14+ days; referral request",
                        contact.companyId(), null, null, contact.id(), null, null,
                        true, "REFERRAL_REQUEST");
            }
        }
    }

    /**
     * Generate follow-up reminders based on application stages and dates.
     *
     * Cadence:
     * T+0/T+1: recruiter note for P1 companies
     * T+1/T+2: referral request
     * T+4/T+5: EM message if no response
     * T+7/T+10: short follow-up
     * T+14: second follow-up or stale
     * T+21: mark ghosted or final follow-up
     */
    private void generateFollowUpReminders(UUID userId, LocalDate today) {
        var applications = applicationRepository.findByUserId(userId);

        for (var app : applications) {
            // Skip terminal states
            if (isTerminal(app.stage())) continue;

            // Get company priority
            String priorityTier = "P2";
            if (app.companyId() != null) {
                var company = companyRepository.findById(app.companyId(), userId).orElse(null);
                if (company != null) priorityTier = company.priorityTier();
            }

            // Applied but no recruiter contact — remind at T+1 for P1, T+3 for all
            if ("APPLIED".equals(app.stage()) && !app.recruiterContacted()) {
                if (app.dateApplied() != null) {
                    var appliedDate = app.dateApplied();
                    var daysSinceApply = today.toEpochDay() - appliedDate.toEpochDay();
                    if (daysSinceApply > 10) {
                        createActionIfNotExists(userId, "FOLLOW_UP_RECRUITER", "P0", 96,
                                today.minusDays(1).toString(),
                                "Applied 10+ days ago, no recruiter contact — send follow-up now",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "FOLLOW_UP");
                    } else if (("P1".equals(priorityTier) || "P1_5".equals(priorityTier))
                            && daysSinceApply >= 1 && daysSinceApply < 3) {
                        createActionIfNotExists(userId, "CONTACT_RECRUITER", "P1", 85,
                                today.toString(),
                                "Applied to P1 company — contact recruiter within 24h to get on radar",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "LINKEDIN_RECRUITER_DM");
                    } else if (daysSinceApply >= 3 && daysSinceApply < 7) {
                        var score = "P1".equals(priorityTier) ? 80 : 70;
                        createActionIfNotExists(userId, "CONTACT_RECRUITER", "P2", score,
                                today.toString(),
                                "Applied 3+ days ago, no recruiter contact yet — send a quick note",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "LINKEDIN_RECRUITER_DM");
                    }
                }
            }

            // HM contacted but no reply after 5 days — follow up
            if ("HIRING_MANAGER_CONTACTED".equals(app.stage())) {
                var lastContact = app.lastContactDate() != null ? app.lastContactDate() : app.dateApplied();
                if (lastContact != null) {
                    var daysSince = today.toEpochDay() - lastContact.toEpochDay();
                    if (daysSince >= 5) {
                        var followDue = lastContact.plusDays(7);
                        var score = !today.isBefore(followDue) ? 85 : 65;
                        createActionIfNotExists(userId, "FOLLOW_UP_MANAGER", "P2", score,
                                followDue.toString(),
                                "HM contacted " + daysSince + " days ago — polite follow-up (due " + followDue + ")",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "FOLLOW_UP");
                    }
                }
            }

            // T+7 follow-up if no response
            if ("APPLIED".equals(app.stage()) || "RECRUITER_CONTACTED".equals(app.stage())) {
                var lastContact = app.lastContactDate() != null ? app.lastContactDate()
                        : app.dateApplied();
                if (lastContact != null) {
                    var daysSince = today.toEpochDay() - lastContact.toEpochDay();
                    if (daysSince >= 7 && daysSince < 14) {
                        var score = "P1".equals(priorityTier) ? 75 : 60;
                        createActionIfNotExists(userId, "FOLLOW_UP_RECRUITER", "P2", score,
                                today.toString(),
                                "No response in 7+ days — send polite follow-up",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "FOLLOW_UP");
                    } else if (daysSince >= 14 && daysSince < 21) {
                        createActionIfNotExists(userId, "FOLLOW_UP_RECRUITER", "P2", 55,
                                today.toString(),
                                "14+ days without response — second follow-up or consider ghosted",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "FOLLOW_UP");
                    } else if (daysSince >= 21) {
                        createActionIfNotExists(userId, "MARK_GHOSTED", "P2", 45,
                                today.toString(),
                                "21+ days without response — mark as ghosted or send final follow-up",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                false, null);
                    }
                }
            }

            if ("RECRUITER_SCREEN_DONE".equals(app.stage())) {
                var ref = app.lastContactDate() != null ? app.lastContactDate() : app.dateApplied();
                if (ref != null) {
                    var daysSince = today.toEpochDay() - ref.toEpochDay();
                    if (daysSince >= 5) {
                        createActionIfNotExists(userId, "FOLLOW_UP_RECRUITER", "P2", 62,
                                today.toString(),
                                "Recruiter screen done " + daysSince + " days ago — ask for next steps",
                                app.companyId(), app.vacancyId(), app.id(), null, null, null,
                                true, "FOLLOW_UP");
                    }
                }
            }

            // Interview prep if screen/interview within 2 days (uses application.next_action_date)
            if ("RECRUITER_SCREEN_SCHEDULED".equals(app.stage()) && app.nextActionDate() != null) {
                long until = app.nextActionDate().toEpochDay() - today.toEpochDay();
                if (until >= 0 && until <= 2) {
                    createActionIfNotExists(userId, "PREPARE_RECRUITER_SCREEN", "P1", 88,
                            today.toString(),
                            "Recruiter screen coming up — prepare answers and positioning",
                            app.companyId(), app.vacancyId(), app.id(), null, null, null,
                            false, null);
                }
            }

            if ("TECHNICAL_INTERVIEW_SCHEDULED".equals(app.stage()) && app.nextActionDate() != null) {
                long until = app.nextActionDate().toEpochDay() - today.toEpochDay();
                if (until >= 0 && until <= 2) {
                    createActionIfNotExists(userId, "PREPARE_TECH_INTERVIEW", "P1", 90,
                            today.toString(),
                            "Technical interview within 2 days — review system design and stack depth",
                            app.companyId(), app.vacancyId(), app.id(), null, null, null,
                            false, null);
                }
            }

            // Post-offer thank-you
            if ("OFFER".equals(app.stage())) {
                createActionIfNotExists(userId, "SEND_POST_INTERVIEW_THANK_YOU", "P1", 80,
                        today.toString(),
                        "Offer stage — send thank-you / recap if not sent yet",
                        app.companyId(), app.vacancyId(), app.id(), null, null, null,
                        true, "POST_INTERVIEW_THANK_YOU");
            }
        }
    }

    /**
     * Generate saved search check reminders when overdue.
     */
    private void generateSavedSearchReminders(UUID userId, LocalDate today) {
        var searches = dsl.selectFrom(table("saved_searches"))
                .where(field("user_id").eq(userId))
                .and(field("active").eq(true))
                .fetch();

        for (var s : searches) {
            var nextCheck = s.get(field("next_check_date"));
            if (nextCheck == null) {
                // Never checked — remind today
                var searchId = s.get(field("id", UUID.class));
                var title = s.get(field("title", String.class));
                createActionIfNotExists(userId, "CHECK_SAVED_SEARCH", "P3", 40,
                        today.toString(),
                        "Run saved search: " + title,
                        null, null, null, null, null, searchId,
                        false, null);
            } else {
                var nextCheckDate = LocalDate.parse(nextCheck.toString());
                if (!today.isBefore(nextCheckDate)) {
                    var searchId = s.get(field("id", UUID.class));
                    var title = s.get(field("title", String.class));
                    createActionIfNotExists(userId, "CHECK_SAVED_SEARCH", "P3", 40,
                            today.toString(),
                            "Run saved search: " + title,
                            null, null, null, null, null, searchId,
                            false, null);
                }
            }
        }
    }

    /**
     * Generate weekly target diagnostic reminders.
     * If user is behind weekly targets by Thursday, create remediation actions.
     */
    private void generateWeeklyTargetReminders(UUID userId, LocalDate today) {
        // Only run on Thursday/Friday when behind target
        var dayOfWeek = today.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        if (dayOfWeek < 4) return; // Only Thu+

        var profile = profileRepository.findByUserId(userId).orElse(null);
        int searchWeek = 1;
        if (profile != null && profile.searchStartDate() != null) {
            long days = today.toEpochDay() - profile.searchStartDate().toEpochDay();
            searchWeek = Math.max(1, (int) (days / 7) + 1);
        }

        // Target: 15-18 applications in week 1, 18-20 thereafter
        int appTarget = searchWeek == 1 ? 15 : 18;

        var weekStartInstant = DbTime.weekStartInstantMondayBerlin(today);
        var thisWeekApps = dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("created_at").greaterOrEqual(weekStartInstant))
        );

        if (thisWeekApps < appTarget * 0.6) {
            // Below 60% of target by Thursday — create alert
            createActionIfNotExists(userId, "ADD_NEW_JOBS", "P1", 80,
                    today.toString(),
                    String.format("Weekly target: %d applications. Current: %d. Add and analyze new jobs to stay on track.",
                            appTarget, thisWeekApps),
                    null, null, null, null, null, null,
                    false, null);
        }

        // City scope expansion — if Munich-heavy after week 2
        if (searchWeek >= 2) {
            var munichApps = dsl.fetchCount(
                    table("applications"),
                    field("user_id").eq(userId)
                            .and(field("city_category").eq("MUNICH"))
            );
            var totalApps = dsl.fetchCount(
                    table("applications"),
                    field("user_id").eq(userId)
            );
            if (totalApps > 10 && munichApps * 100 / totalApps > 80) {
                createActionIfNotExists(userId, "EXPAND_CITY_SCOPE", "P2", 65,
                        today.toString(),
                        "80%+ of applications are Munich-only. Expand to Berlin, Hamburg, Frankfurt to improve response rate.",
                        null, null, null, null, null, null,
                        false, null);
            }
        }
    }

    /**
     * Remind about vacancies that were analyzed as APPLY/MAYBE but not yet added to pipeline.
     */
    private void generateDecisionReminders(UUID userId, LocalDate today) {
        var pendingDecisions = dsl.selectFrom(table("vacancies"))
                .where(field("user_id").eq(userId))
                .and(field("status").in("SHOULD_APPLY", "MAYBE"))
                .and(field("created_at").lessThan(DbTime.startOfDayBerlin(today)))
                .fetch();

        for (var v : pendingDecisions) {
            var vacancyId = v.get(field("id", UUID.class));
            var title = v.get(field("title", String.class));
            createActionIfNotExists(userId, "DECIDE_ADD_TO_PIPELINE", "P2", 70,
                    today.toString(),
                    "Analyzed job pending decision: " + title,
                    v.get(field("company_id", UUID.class)), vacancyId, null, null, null, null,
                    false, null);
        }
    }

    private void generateRejectionAnalysisReminders(UUID userId, LocalDate today) {
        for (var app : applicationRepository.findByUserId(userId)) {
            if (!"REJECTED".equals(app.stage())) {
                continue;
            }
            if (app.rejectionReason() != null && !app.rejectionReason().isBlank()) {
                continue;
            }
            createActionIfNotExists(userId, "ANALYZE_REJECTION", "P3", 40,
                    today.toString(),
                    "Rejected application — log why for strategy review",
                    app.companyId(), app.vacancyId(), app.id(), null, null, null,
                    false, null);
        }
    }

    /**
     * Trigger recalculation after a specific application event.
     */
    public void triggerForApplication(UUID userId, UUID applicationId) {
        // Targeted recalculation — for now run full recalc
        recalculateForUser(userId);
    }

    private void createActionIfNotExists(
            UUID userId, String actionType, String priority, int priorityScore,
            String dueDate, String reason,
            UUID companyId, UUID vacancyId, UUID applicationId, UUID contactId,
            UUID messageId, UUID savedSearchId,
            boolean generateMessage, String messageType
    ) {
        if (nextActionRepository.existsPendingAction(userId, actionType, companyId, vacancyId, applicationId,
                contactId, savedSearchId)) {
            return;
        }
        nextActionRepository.save(new NextActionRepository.ActionRow(
                null, userId, actionType, priority, priorityScore, dueDate,
                "PENDING", reason, companyId, vacancyId, applicationId, contactId,
                messageId, savedSearchId, generateMessage, messageType, null, null, null, null
        ));
    }

    private static boolean relationshipStrengthAtLeast(String strength, int minNumeric) {
        return strengthScore(strength) >= minNumeric;
    }

    private static int strengthScore(String strength) {
        if (strength == null || strength.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(strength.trim());
        } catch (NumberFormatException ignored) {
            return switch (strength.trim().toUpperCase()) {
                case "HOT", "CONNECTED" -> 5;
                case "WARM" -> 3;
                case "LUKEWARM" -> 2;
                default -> 0;
            };
        }
    }

    private static Integer daysSinceLastContact(String lastContactedDate, LocalDate today) {
        if (lastContactedDate == null || lastContactedDate.isBlank()) {
            return null;
        }
        try {
            var d = LocalDate.parse(lastContactedDate);
            return (int) (today.toEpochDay() - d.toEpochDay());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isTerminal(String stage) {
        return stage != null && (stage.equals("OFFER") || stage.equals("REJECTED")
                || stage.equals("GHOSTED") || stage.equals("WITHDRAWN") || stage.equals("ARCHIVED"));
    }
}
