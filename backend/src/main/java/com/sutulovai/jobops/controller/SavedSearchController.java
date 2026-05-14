package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.response.SavedSearchResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.SavedSearchRepository;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/searches")
@Tag(name = "Saved Searches")
public class SavedSearchController {

    private final SavedSearchRepository savedSearchRepository;

    public SavedSearchController(SavedSearchRepository savedSearchRepository) {
        this.savedSearchRepository = savedSearchRepository;
    }

    @GetMapping
    public ResponseEntity<List<SavedSearchResponse>> list() {
        return ResponseEntity.ok(
                savedSearchRepository.findByUserId(SecurityUtils.currentUserId())
                        .stream().map(this::toResponse).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavedSearchResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
                savedSearchRepository.findById(id, SecurityUtils.currentUserId())
                        .map(this::toResponse)
                        .orElseThrow(() -> NotFoundException.forEntity("SavedSearch", id))
        );
    }

    @PostMapping("/{id}/checked")
    public ResponseEntity<Void> markChecked(@PathVariable UUID id) {
        var userId = SecurityUtils.currentUserId();
        var search = savedSearchRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("SavedSearch", id));
        var nextCheck = "DAILY".equals(search.frequency())
                ? LocalDate.now().plusDays(1).toString()
                : LocalDate.now().plusWeeks(1).toString();
        savedSearchRepository.markChecked(id, userId, nextCheck);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        savedSearchRepository.findById(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> NotFoundException.forEntity("SavedSearch", id));
        savedSearchRepository.delete(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    private SavedSearchResponse toResponse(SavedSearchRepository.SearchRow s) {
        double responseRate = s.applicationsCreatedCount() > 0
                ? (s.responsesFromCount() * 100.0 / s.applicationsCreatedCount())
                : 0.0;
        return new SavedSearchResponse(
                s.id(), s.userId(), s.title(), s.platform(),
                s.url(), s.queryText(), s.booleanQuery(),
                s.city(),
                s.keywords() != null ? Arrays.asList(s.keywords()) : List.of(),
                s.frequency(), s.lastCheckedDate(), s.nextCheckDate(),
                s.useful(), s.yieldRating(),
                s.jobsAddedCount(), s.applicationsCreatedCount(),
                s.applicationsCreatedCount() > 0 ? responseRate : null,
                s.notes(), s.active(), s.createdAt(), s.updatedAt()
        );
    }
}
