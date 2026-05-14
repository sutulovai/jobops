package com.sutulovai.jobops.storage;

import java.io.InputStream;
import java.util.UUID;

public interface FileStorageService {

    /**
     * Store a file and return its storage path (relative to root).
     */
    String store(UUID userId, String category, String filename, InputStream content, String contentType);

    /**
     * Return input stream for a stored file.
     */
    InputStream retrieve(String storagePath);

    /**
     * Delete a stored file.
     */
    void delete(String storagePath);
}
