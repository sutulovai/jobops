package com.sutulovai.jobops.repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<UserRow> findById(UUID id);
    Optional<UserRow> findByEmail(String email);
    boolean existsByEmail(String email);
    UserRow save(UserRow user);

    record UserRow(UUID id, String email, String passwordHash, String openaiApiKey) {}
}
