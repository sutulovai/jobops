package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.response.CvResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.CvRepository;
import com.sutulovai.jobops.repository.jooq.JooqCvRepository;
import com.sutulovai.jobops.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class CvService {

    private static final Logger log = LoggerFactory.getLogger(CvService.class);

    private final CvRepository cvRepository;
    private final JooqCvRepository jooqCvRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractor pdfTextExtractor;

    public CvService(CvRepository cvRepository, JooqCvRepository jooqCvRepository,
                     FileStorageService fileStorageService, PdfTextExtractor pdfTextExtractor) {
        this.cvRepository = cvRepository;
        this.jooqCvRepository = jooqCvRepository;
        this.fileStorageService = fileStorageService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    public List<CvResponse> listCvs(UUID userId) {
        return cvRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public CvResponse uploadCv(UUID userId, MultipartFile file, String label) throws IOException {
        log.info("🔵 Uploading CV for user {}: {}", userId, file.getOriginalFilename());

        var storagePath = fileStorageService.store(
                userId, "cvs", file.getOriginalFilename(),
                file.getInputStream(), file.getContentType()
        );

        var existingCvs = cvRepository.findByUserId(userId);
        var isFirstCv = existingCvs.isEmpty();
        var version = existingCvs.size() + 1;

        var cvRow = cvRepository.save(new CvRepository.CvRow(
                null, userId,
                label != null && !label.isBlank() ? label : "CV v" + version,
                version,
                isFirstCv,
                file.getOriginalFilename(),
                storagePath,
                file.getContentType() != null ? file.getContentType() : "application/pdf",
                file.getSize(),
                null, null
        ));

        // Extract text asynchronously — write back to DB
        try (var inputStream = fileStorageService.retrieve(storagePath)) {
            var text = pdfTextExtractor.extract(inputStream);
            if (!text.isBlank()) {
                jooqCvRepository.updateExtractedText(cvRow.id(), text);
            }
        } catch (Exception e) {
            log.warn("⚠️ Text extraction failed for CV {}: {}", cvRow.id(), e.getMessage());
        }

        log.info("✅ CV uploaded: {}", cvRow.id());
        return toResponse(cvRow);
    }

    public CvResponse setDefault(UUID id, UUID userId) {
        var cv = cvRepository.findById(id)
                .filter(r -> r.userId().equals(userId))
                .orElseThrow(() -> NotFoundException.forEntity("CV", id));
        cvRepository.clearDefaultForUser(userId);
        cvRepository.setDefault(id, userId);
        return toResponse(new CvRepository.CvRow(cv.id(), cv.userId(), cv.label(), cv.version(),
                true, cv.originalFilename(), cv.storagePath(), cv.mimeType(), cv.fileSizeBytes(),
                cv.createdAt(), cv.updatedAt()));
    }

    public void deleteCv(UUID id, UUID userId) {
        var cv = cvRepository.findById(id)
                .filter(r -> r.userId().equals(userId))
                .orElseThrow(() -> NotFoundException.forEntity("CV", id));
        fileStorageService.delete(cv.storagePath());
        cvRepository.delete(id, userId);
        log.info("✅ CV deleted: {}", id);
    }

    public String getExtractedText(UUID id, UUID userId) {
        cvRepository.findById(id)
                .filter(r -> r.userId().equals(userId))
                .orElseThrow(() -> NotFoundException.forEntity("CV", id));
        return cvRepository.findExtractedText(id);
    }

    private CvResponse toResponse(CvRepository.CvRow cv) {
        return new CvResponse(
                cv.id(), cv.userId(), cv.label(), cv.version(), cv.isDefault(),
                cv.originalFilename(), cv.fileSizeBytes() != null ? cv.fileSizeBytes() : 0,
                cv.createdAt(), cv.updatedAt()
        );
    }
}
