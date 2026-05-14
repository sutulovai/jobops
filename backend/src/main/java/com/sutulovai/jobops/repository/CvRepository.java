package com.sutulovai.jobops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CvRepository {
    List<CvRow> findByUserId(UUID userId);
    Optional<CvRow> findById(UUID id);
    Optional<CvRow> findDefaultByUserId(UUID userId);
    CvRow save(CvRow cv);
    void clearDefaultForUser(UUID userId);
    void setDefault(UUID id, UUID userId);
    void delete(UUID id, UUID userId);
    String findExtractedText(UUID id);

    record CvRow(
            UUID id,
            UUID userId,
            String label,
            int version,
            boolean isDefault,
            String originalFilename,
            String storagePath,
            String mimeType,
            Long fileSizeBytes,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
