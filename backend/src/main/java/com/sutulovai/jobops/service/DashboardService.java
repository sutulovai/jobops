package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.response.DashboardSummaryResponse;
import com.sutulovai.jobops.dto.response.NextActionResponse;
import com.sutulovai.jobops.repository.*;
import com.sutulovai.jobops.util.DbTime;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final NextActionRepository nextActionRepository;
    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final ProfileRepository profileRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final DSLContext dsl;

    public DashboardService(
            NextActionRepository nextActionRepository,
            ApplicationRepository applicationRepository,
            VacancyRepository vacancyRepository,
            ProfileRepository profileRepository,
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            DSLContext dsl
    ) {
        this.nextActionRepository = nextActionRepository;
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.dsl = dsl;
    }

    public DashboardSummaryResponse getSummary(UUID userId) {
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));

        // Today + overdue actions (sorted: overdue by oldest due first, then priority)
        var todayActions = sortActionsForDashboard(nextActionRepository.findTodayAndOverdue(userId), today);
        var overdueCount = (int) todayActions.stream()
                .filter(a -> a.dueDate() != null && LocalDate.parse(a.dueDate()).isBefore(today))
                .count();
        var todayCount = todayActions.size() - overdueCount;

        // Pending decisions (SHOULD_APPLY or MAYBE vacancies not yet in pipeline)
        var pendingDecisions = dsl.fetchCount(
                table("vacancies"),
                field("user_id").eq(userId).and(field("status").in("SHOULD_APPLY", "MAYBE"))
        );

        // Active applications
        var activeApps = applicationRepository.findByUserId(userId).stream()
                .filter(a -> !isTerminal(a.stage()))
                .toList();

        // Applications by stage
        var byStage = new LinkedHashMap<String, Integer>();
        for (var a : activeApps) {
            byStage.merge(a.stage(), 1, Integer::sum);
        }

        // Weekly progress
        var weeklyProgress = calculateWeeklyProgress(userId, today);

        // Batch-resolve names for all today/overdue actions
        var companyNames = resolveCompanyNames(userId, todayActions);
        var vacancyTitles = resolveVacancyTitles(userId, todayActions);
        var contactNames = resolveContactNames(userId, todayActions);

        var topActions = todayActions.stream()
                .map(a -> new NextActionResponse(
                        a.id(), a.userId(), a.actionType(), a.priority(), a.priorityScore(),
                        a.dueDate(), a.status(), a.reason(),
                        a.companyId(), companyNames.get(a.companyId()),
                        a.vacancyId(), vacancyTitles.get(a.vacancyId()),
                        a.applicationId(),
                        a.contactId(), contactNames.get(a.contactId()),
                        a.messageId(), a.savedSearchId(),
                        a.generatedMessageRequired(), a.recommendedMessageType(),
                        a.snoozedUntil(), a.skippedUntil(), a.createdAt(), a.completedAt()
                ))
                .toList();

        var responseRate = computeResponseRatePercent(userId, today);

        return new DashboardSummaryResponse(
                todayCount,
                overdueCount,
                pendingDecisions,
                activeApps.size(),
                byStage,
                weeklyProgress,
                responseRate,
                topActions
        );
    }

    public static List<NextActionRepository.ActionRow> sortActionsForDashboard(
            List<NextActionRepository.ActionRow> actions,
            LocalDate todayBerlin
    ) {
        var list = new ArrayList<>(actions);
        list.sort((a, b) -> {
            if (a.dueDate() == null && b.dueDate() == null) {
                return Integer.compare(b.priorityScore(), a.priorityScore());
            }
            if (a.dueDate() == null) return 1;
            if (b.dueDate() == null) return -1;
            var da = LocalDate.parse(a.dueDate());
            var db = LocalDate.parse(b.dueDate());
            boolean oa = da.isBefore(todayBerlin);
            boolean ob = db.isBefore(todayBerlin);
            if (oa != ob) {
                return oa ? -1 : 1;
            }
            if (oa) {
                int byDue = da.compareTo(db);
                if (byDue != 0) {
                    return byDue;
                }
            }
            int ps = Integer.compare(b.priorityScore(), a.priorityScore());
            if (ps != 0) {
                return ps;
            }
            return da.compareTo(db);
        });
        return list;
    }

    private Map<UUID, String> resolveCompanyNames(UUID userId, List<NextActionRepository.ActionRow> actions) {
        var ids = actions.stream().map(NextActionRepository.ActionRow::companyId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        var result = new HashMap<UUID, String>();
        for (var id : ids) {
            companyRepository.findById(id, userId).ifPresent(c -> result.put(id, c.name()));
        }
        return result;
    }

    private Map<UUID, String> resolveVacancyTitles(UUID userId, List<NextActionRepository.ActionRow> actions) {
        var ids = actions.stream().map(NextActionRepository.ActionRow::vacancyId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        var result = new HashMap<UUID, String>();
        for (var id : ids) {
            vacancyRepository.findById(id, userId).ifPresent(v -> result.put(id, v.title()));
        }
        return result;
    }

    private Map<UUID, String> resolveContactNames(UUID userId, List<NextActionRepository.ActionRow> actions) {
        var ids = actions.stream().map(NextActionRepository.ActionRow::contactId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        var result = new HashMap<UUID, String>();
        for (var id : ids) {
            contactRepository.findById(id, userId).ifPresent(c -> result.put(id, c.name()));
        }
        return result;
    }

    private DashboardSummaryResponse.WeeklyProgressResponse calculateWeeklyProgress(UUID userId, LocalDate today) {
        var profile = profileRepository.findByUserId(userId).orElse(null);
        int weekNumber = 1;
        if (profile != null && profile.searchStartDate() != null) {
            long days = today.toEpochDay() - profile.searchStartDate().toEpochDay();
            weekNumber = Math.max(1, (int) (days / 7) + 1);
        }

        // Targets
        int appTarget = weekNumber == 1 ? 15 : 18;
        int recruiterTarget = weekNumber == 1 ? 12 : 10;
        int managerTarget = weekNumber == 1 ? 8 : 10;
        int referralTarget = weekNumber == 1 ? 10 : 10;
        int analyzeTarget = 20;

        // Actuals for this week (Monday 00:00 Berlin → now)
        var weekStart = DbTime.weekStartInstantMondayBerlin(today);
        var weekStartDay = today.with(DayOfWeek.MONDAY);

        var appsDone = dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("date_applied").isNotNull())
                        .and(field("date_applied").greaterOrEqual(weekStartDay))
                        .and(field("stage").ne("ADDED_TO_PIPELINE"))
        );

        var vacancySubselect = dsl.select(field("id", UUID.class))
                .from(table("vacancies"))
                .where(field("user_id").eq(userId));
        var analyzesDone = dsl.fetchCount(
                table("job_analyses"),
                field("vacancy_id").in(vacancySubselect)
                        .and(field("created_at").greaterOrEqual(weekStart))
        );

        var recruiterTypes = List.of("LINKEDIN_RECRUITER_DM", "EMAIL_RECRUITER", "FOLLOW_UP");
        var recruiterMsgsFromOutreach = dsl.fetchCount(
                table("outreach_messages"),
                field("user_id").eq(userId)
                        .and(field("message_type").in(recruiterTypes))
                        .and(field("status").eq("SENT"))
                        .and(coalesce(field("sent_at"), field("created_at")).greaterOrEqual(weekStart))
        );
        var recruiterMsgsFromApps = dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("recruiter_contacted").eq(true))
                        .and(field("updated_at").greaterOrEqual(weekStart))
        );
        var recruiterMsgsDone = recruiterMsgsFromOutreach + recruiterMsgsFromApps;

        var managerMsgsFromOutreach = dsl.fetchCount(
                table("outreach_messages"),
                field("user_id").eq(userId)
                        .and(field("message_type").eq("LINKEDIN_MANAGER_DM"))
                        .and(field("status").eq("SENT"))
                        .and(coalesce(field("sent_at"), field("created_at")).greaterOrEqual(weekStart))
        );
        var managerMsgsFromApps = dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("hiring_manager_contacted").eq(true))
                        .and(field("updated_at").greaterOrEqual(weekStart))
        );
        var managerMsgsDone = managerMsgsFromOutreach + managerMsgsFromApps;

        var referralsFromOutreach = dsl.fetchCount(
                table("outreach_messages"),
                field("user_id").eq(userId)
                        .and(field("message_type").eq("REFERRAL_REQUEST"))
                        .and(field("status").eq("SENT"))
                        .and(coalesce(field("sent_at"), field("created_at")).greaterOrEqual(weekStart))
        );
        var referralsFromApps = dsl.fetchCount(
                table("applications"),
                field("user_id").eq(userId)
                        .and(field("referral_requested").eq(true))
                        .and(field("updated_at").greaterOrEqual(weekStart))
        );
        var referralsDone = referralsFromOutreach + referralsFromApps;

        return new DashboardSummaryResponse.WeeklyProgressResponse(
                weekNumber, appTarget, appsDone,
                recruiterTarget, recruiterMsgsDone,
                managerTarget, managerMsgsDone,
                referralTarget, referralsDone,
                analyzeTarget, analyzesDone
        );
    }

    private Integer computeResponseRatePercent(UUID userId, LocalDate today) {
        var zone = ZoneId.of("Europe/Berlin");
        var apps = applicationRepository.findByUserId(userId);
        int eligible = 0;
        int progressed = 0;
        for (var a : apps) {
            LocalDate refDate = a.dateApplied() != null
                    ? a.dateApplied()
                    : LocalDate.ofInstant(a.createdAt(), zone);
            long ageDays = today.toEpochDay() - refDate.toEpochDay();
            if (ageDays <= 7) {
                continue;
            }
            eligible++;
            if (!"ADDED_TO_PIPELINE".equals(a.stage()) && !"APPLIED".equals(a.stage())) {
                progressed++;
            }
        }
        if (eligible == 0) {
            return null;
        }
        return (int) Math.round(100.0 * progressed / eligible);
    }

    private static boolean isTerminal(String stage) {
        return stage != null && (stage.equals("OFFER") || stage.equals("REJECTED")
                || stage.equals("GHOSTED") || stage.equals("WITHDRAWN") || stage.equals("ARCHIVED"));
    }
}
