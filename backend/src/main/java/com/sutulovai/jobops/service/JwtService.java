package com.sutulovai.jobops.service;

import com.sutulovai.jobops.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = props.accessTokenExpirationMs();
        this.refreshExpirationMs = props.refreshTokenExpirationMs();
    }

    public String issueAccessToken(UUID userId) {
        return issue(userId, TYPE_ACCESS, accessExpirationMs);
    }

    public String issueRefreshToken(UUID userId) {
        return issue(userId, TYPE_REFRESH, refreshExpirationMs);
    }

    private String issue(UUID userId, String type, long expirationMs) {
        var now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public UUID validateAccessToken(String token) {
        return extractUserId(token, TYPE_ACCESS);
    }

    public UUID validateRefreshToken(String token) {
        return extractUserId(token, TYPE_REFRESH);
    }

    private UUID extractUserId(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String type = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                throw new JwtException("Token type mismatch: expected " + expectedType);
            }
            return UUID.fromString(claims.getSubject());
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }
}
