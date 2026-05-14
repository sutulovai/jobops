package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.CreateApplicationRequest;
import com.sutulovai.jobops.dto.response.ApplicationResponse;
import com.sutulovai.jobops.service.ApplicationService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> list() {
        return ResponseEntity.ok(applicationService.listApplications(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getApplication(id, SecurityUtils.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.addToPipeline(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/{id}/advance")
    public ResponseEntity<ApplicationResponse> advanceStage(
            @PathVariable UUID id,
            @RequestBody StageRequest request
    ) {
        return ResponseEntity.ok(applicationService.advanceStage(id, SecurityUtils.currentUserId(), request.stage()));
    }

    @PostMapping("/{id}/follow-up")
    public ResponseEntity<Void> recordFollowUp(@PathVariable UUID id) {
        applicationService.recordFollowUp(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApplicationResponse> updateNotes(
            @PathVariable UUID id,
            @RequestBody NotesRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateNotes(id, SecurityUtils.currentUserId(), request.notes()));
    }

    public record StageRequest(String stage) {}
    public record NotesRequest(String notes) {}
}
