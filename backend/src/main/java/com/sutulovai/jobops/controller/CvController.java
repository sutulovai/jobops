package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.response.CvResponse;
import com.sutulovai.jobops.service.CvService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cvs")
@Tag(name = "CV")
public class CvController {

    private final CvService cvService;

    public CvController(CvService cvService) {
        this.cvService = cvService;
    }

    @GetMapping
    @Operation(summary = "List CVs")
    public ResponseEntity<List<CvResponse>> list() {
        return ResponseEntity.ok(cvService.listCvs(SecurityUtils.currentUserId()));
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Upload a CV")
    public ResponseEntity<CvResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "label", required = false) String label
    ) throws IOException {
        return ResponseEntity.ok(cvService.uploadCv(SecurityUtils.currentUserId(), file, label));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set CV as default")
    public ResponseEntity<CvResponse> setDefault(@PathVariable UUID id) {
        return ResponseEntity.ok(cvService.setDefault(id, SecurityUtils.currentUserId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete CV")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cvService.deleteCv(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/text")
    @Operation(summary = "Get extracted text from CV")
    public ResponseEntity<String> getText(@PathVariable UUID id) {
        return ResponseEntity.ok(cvService.getExtractedText(id, SecurityUtils.currentUserId()));
    }
}
