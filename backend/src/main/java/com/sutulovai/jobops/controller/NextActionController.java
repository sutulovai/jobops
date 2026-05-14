package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.CompleteActionRequest;
import com.sutulovai.jobops.dto.response.NextActionResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.CompanyRepository;
import com.sutulovai.jobops.repository.ContactRepository;
import com.sutulovai.jobops.repository.NextActionRepository;
import com.sutulovai.jobops.repository.VacancyRepository;
import com.sutulovai.jobops.service.action.NextActionEngine;
import com.sutulovai.jobops.service.action.NextActionService;
import com.sutulovai.jobops.service.DashboardService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/actions")
@Tag(name = "Next Actions")
public class NextActionController {

    private final NextActionRepository nextActionRepository;
    private final NextActionEngine nextActionEngine;
    private final NextActionService nextActionService;
    private final CompanyRepository companyRepository;
    private final VacancyRepository vacancyRepository;
    private final ContactRepository contactRepository;

    public NextActionController(NextActionRepository nextActionRepository,
                                NextActionEngine nextActionEngine,
                                NextActionService nextActionService,
                                CompanyRepository companyRepository,
                                VacancyRepository vacancyRepository,
                                ContactRepository contactRepository) {
        this.nextActionRepository = nextActionRepository;
        this.nextActionEngine = nextActionEngine;
        this.nextActionService = nextActionService;
        this.companyRepository = companyRepository;
        this.vacancyRepository = vacancyRepository;
        this.contactRepository = contactRepository;
    }

    @GetMapping("/today")
    public ResponseEntity<List<NextActionResponse>> today() {
        var userId = SecurityUtils.currentUserId();
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));
        var actions = DashboardService.sortActionsForDashboard(
                nextActionRepository.findTodayAndOverdue(userId), today);
        return ResponseEntity.ok(actions.stream().map(a -> toResponseResolved(a, userId)).toList());
    }

    @GetMapping
    public ResponseEntity<List<NextActionResponse>> all() {
        var userId = SecurityUtils.currentUserId();
        var today = LocalDate.now(ZoneId.of("Europe/Berlin"));
        var actions = DashboardService.sortActionsForDashboard(
                nextActionRepository.findPending(userId), today);
        return ResponseEntity.ok(actions.stream().map(a -> toResponseResolved(a, userId)).toList());
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<Void> markDone(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteActionRequest body
    ) {
        String text = body != null ? body.messageText() : null;
        nextActionService.completeDone(id, SecurityUtils.currentUserId(), text);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<Void> skip(@PathVariable UUID id) {
        nextActionRepository.findById(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> NotFoundException.forEntity("Action", id));
        nextActionRepository.skip(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/snooze")
    public ResponseEntity<Void> snooze(@PathVariable UUID id, @RequestBody SnoozeRequest req) {
        nextActionRepository.findById(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> NotFoundException.forEntity("Action", id));
        nextActionRepository.snooze(id, SecurityUtils.currentUserId(), req.snoozeUntil());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculate() {
        nextActionEngine.recalculateForUser(SecurityUtils.currentUserId());
        return ResponseEntity.ok().build();
    }

    private NextActionResponse toResponseResolved(NextActionRepository.ActionRow a, UUID userId) {
        String companyName = a.companyId() != null
                ? companyRepository.findById(a.companyId(), userId).map(c -> c.name()).orElse(null) : null;
        String vacancyTitle = a.vacancyId() != null
                ? vacancyRepository.findById(a.vacancyId(), userId).map(v -> v.title()).orElse(null) : null;
        String contactName = a.contactId() != null
                ? contactRepository.findById(a.contactId(), userId).map(c -> c.name()).orElse(null) : null;
        return new NextActionResponse(
                a.id(), a.userId(), a.actionType(), a.priority(), a.priorityScore(),
                a.dueDate(), a.status(), a.reason(),
                a.companyId(), companyName,
                a.vacancyId(), vacancyTitle,
                a.applicationId(), a.contactId(), contactName,
                a.messageId(), a.savedSearchId(),
                a.generatedMessageRequired(), a.recommendedMessageType(),
                a.snoozedUntil(), a.skippedUntil(), a.createdAt(), a.completedAt()
        );
    }

    public record SnoozeRequest(String snoozeUntil) {}
}
