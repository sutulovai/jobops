package com.sutulovai.jobops.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CvResponse(
        UUID id,
        UUID userId,
        String label,
        int version,
        boolean isDefault,
        String originalFilename,
        long fileSizeBytes,
        Instant createdAt,
        Instant updatedAt
) {
}
