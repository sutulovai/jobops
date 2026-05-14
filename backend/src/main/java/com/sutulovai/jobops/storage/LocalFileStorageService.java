package com.sutulovai.jobops.storage;

import com.sutulovai.jobops.config.FileStorageProperties;
import com.sutulovai.jobops.exception.ErrorCode;
import com.sutulovai.jobops.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path root;

    public LocalFileStorageService(FileStorageProperties props) {
        this.root = Path.of(props.root());
        try {
            Files.createDirectories(this.root);
            log.info("🔵 File storage initialized at {}", this.root.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file storage root: " + root, e);
        }
    }

    @Override
    public String store(UUID userId, String category, String filename, InputStream content, String contentType) {
        try {
            var dir = root.resolve(userId.toString()).resolve(category);
            Files.createDirectories(dir);
            var safeFilename = Path.of(filename).getFileName().toString();
            var dest = dir.resolve(UUID.randomUUID() + "_" + safeFilename);
            Files.copy(content, dest, StandardCopyOption.REPLACE_EXISTING);
            log.info("💾 Stored file: {}", dest);
            return root.relativize(dest).toString();
        } catch (IOException e) {
            log.error("❌ Failed to store file for user {}", userId, e);
            throw new ValidationException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieve(String storagePath) {
        try {
            return Files.newInputStream(root.resolve(storagePath));
        } catch (IOException e) {
            throw new ValidationException("File not found: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(root.resolve(storagePath));
        } catch (IOException e) {
            log.warn("⚠️ Failed to delete file {}: {}", storagePath, e.getMessage());
        }
    }
}
