package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.UpdateProfileRequest;
import com.sutulovai.jobops.dto.response.ProfileResponse;
import com.sutulovai.jobops.service.ProfileService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile(SecurityUtils.currentUserId()));
    }

    @PutMapping
    @Operation(summary = "Update profile")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(SecurityUtils.currentUserId(), request));
    }
}
