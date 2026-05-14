package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.response.WeeklyReviewResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.service.WeeklyReviewService;
import com.sutulovai.jobops.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly-review")
public class WeeklyReviewController {

    private final WeeklyReviewService service;

    public WeeklyReviewController(WeeklyReviewService service) {
        this.service = service;
    }

    @GetMapping("/latest")
    public ResponseEntity<WeeklyReviewResponse> getLatest() {
        var userId = SecurityUtils.currentUserId();
        return service.getLatest(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("No weekly review found. Generate one first."));
    }

    @PostMapping("/generate")
    public WeeklyReviewResponse generate() {
        var userId = SecurityUtils.currentUserId();
        return service.generateReview(userId);
    }
}
