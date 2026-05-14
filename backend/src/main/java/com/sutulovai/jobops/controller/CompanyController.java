package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.CreateCompanyRequest;
import com.sutulovai.jobops.dto.response.CompanyResponse;
import com.sutulovai.jobops.service.CompanyService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> list() {
        return ResponseEntity.ok(companyService.listCompanies(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id, SecurityUtils.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        return ResponseEntity.ok(companyService.updateCompany(id, SecurityUtils.currentUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.deleteCompany(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
