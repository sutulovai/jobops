package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.request.LoginRequest;
import com.sutulovai.jobops.dto.request.RefreshTokenRequest;
import com.sutulovai.jobops.dto.request.RegisterRequest;
import com.sutulovai.jobops.dto.response.AuthResponse;
import com.sutulovai.jobops.exception.ConflictException;
import com.sutulovai.jobops.exception.UnauthorizedException;
import com.sutulovai.jobops.repository.ProfileRepository;
import com.sutulovai.jobops.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DataSeeder dataSeeder;

    public AuthService(UserRepository userRepository, ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, DataSeeder dataSeeder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dataSeeder = dataSeeder;
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("🔵 Registering user: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw ConflictException.emailTaken(request.email());
        }
        var user = userRepository.save(new UserRepository.UserRow(
                null, request.email(), passwordEncoder.encode(request.password()), null
        ));
        // Create default profile for Artem's Germany job search
        profileRepository.upsert(defaultProfile(user.id()));
        // Seed companies and saved searches
        dataSeeder.seedForUser(user.id());
        log.info("✅ Registered user: {}", user.id());
        return issueTokens(user.id(), user.email());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("🔵 Login attempt: {}", request.email());
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(UnauthorizedException::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw UnauthorizedException.invalidCredentials();
        }
        log.info("✅ Login successful: {}", user.id());
        return issueTokens(user.id(), user.email());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            var userId = jwtService.validateRefreshToken(request.refreshToken());
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            return issueTokens(userId, user.email());
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    private AuthResponse issueTokens(UUID userId, String email) {
        return new AuthResponse(
                jwtService.issueAccessToken(userId),
                jwtService.issueRefreshToken(userId),
                userId,
                email
        );
    }

    private ProfileRepository.ProfileRow defaultProfile(UUID userId) {
        return new ProfileRepository.ProfileRow(
                null, userId,
                "Artem Sutulov",
                null,
                List.of("Germany"),
                List.of("Munich", "Berlin"),
                List.of("Hamburg", "Frankfurt"),
                List.of("Senior Backend Engineer", "Senior Software Engineer",
                        "Backend Platform Engineer", "Payments Engineer", "Fintech Backend Engineer"),
                80000, 95000, 80000, 110000,
                "2-3 months",
                "Relocating from abroad, prepared for EU Blue Card",
                "EU Blue Card prepared",
                "C2", "A2/B1",
                List.of("Fintech", "Payments", "E-commerce", "B2B SaaS", "Mobility", "Platform"),
                List.of("Consulting", "Body leasing"),
                List.of("PRODUCT", "FINTECH", "BANK", "ECOMMERCE", "B2B_SAAS", "MOBILITY"),
                List.of("CONSULTING", "AGENCY"),
                "Senior",
                "Senior Backend Engineer for payments, regulated fintech, transaction-critical distributed systems, reliability, safe migrations, rollback-safe releases, observability, and production ownership. 8+ years Java/Kotlin/Spring Boot, microservices, Kafka, PostgreSQL, Docker/K8s, AWS/GCP.",
                "professional",
                "Europe/Berlin",
                null
        );
    }
}
