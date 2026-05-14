package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.CreateContactRequest;
import com.sutulovai.jobops.dto.response.ContactResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.ContactRepository;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts")
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> list() {
        return ResponseEntity.ok(
                contactRepository.findByUserId(SecurityUtils.currentUserId())
                        .stream().map(c -> toResponse(c, null)).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
                contactRepository.findById(id, SecurityUtils.currentUserId())
                        .map(c -> toResponse(c, null))
                        .orElseThrow(() -> NotFoundException.forEntity("Contact", id))
        );
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody CreateContactRequest request) {
        var userId = SecurityUtils.currentUserId();
        var saved = contactRepository.save(new ContactRepository.ContactRow(
                null, userId, request.companyId(),
                request.name(), request.title(),
                request.contactType() != null ? request.contactType() : "OTHER",
                request.linkedInUrl(), request.email(),
                request.relationshipStrength() != null ? request.relationshipStrength() : "COLD",
                request.source(), null, null,
                request.notes(),
                request.preferredChannel() != null ? request.preferredChannel() : "LINKEDIN",
                request.status() != null ? request.status() : "NEW",
                request.vacancyId(), request.applicationId(), null, null
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateContactRequest request
    ) {
        var userId = SecurityUtils.currentUserId();
        var existing = contactRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Contact", id));
        var saved = contactRepository.save(new ContactRepository.ContactRow(
                id, userId, request.companyId(),
                request.name(), request.title(),
                request.contactType() != null ? request.contactType() : "OTHER",
                request.linkedInUrl(), request.email(),
                request.relationshipStrength() != null ? request.relationshipStrength() : "COLD",
                request.source(),
                existing.lastContactedDate(), existing.nextFollowUpDate(),
                request.notes(),
                request.preferredChannel() != null ? request.preferredChannel() : "LINKEDIN",
                request.status() != null ? request.status() : existing.status(),
                request.vacancyId(), request.applicationId(), null, null
        ));
        return ResponseEntity.ok(toResponse(saved, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactRepository.findById(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> NotFoundException.forEntity("Contact", id));
        contactRepository.delete(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    private ContactResponse toResponse(ContactRepository.ContactRow c, String companyName) {
        return new ContactResponse(
                c.id(), c.userId(), c.companyId(), companyName,
                c.name(), c.title(), c.contactType(),
                c.linkedInUrl(), c.email(), c.relationshipStrength(),
                c.source(), c.lastContactedDate(), c.nextFollowUpDate(),
                c.notes(), c.preferredChannel(), c.status(),
                c.vacancyId(), c.applicationId(), c.createdAt(), c.updatedAt()
        );
    }
}
