package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.UserRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqUserRepository implements UserRepository {

    private final DSLContext dsl;

    public JooqUserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<UserRow> findById(UUID id) {
        return dsl.select(
                        field("id", UUID.class),
                        field("email", String.class),
                        field("password_hash", String.class),
                        field("openai_api_key", String.class)
                )
                .from(table("users"))
                .where(field("id").eq(id))
                .fetchOptional(r -> new UserRow(
                        r.get(field("id", UUID.class)),
                        r.get(field("email", String.class)),
                        r.get(field("password_hash", String.class)),
                        r.get(field("openai_api_key", String.class))
                ));
    }

    @Override
    public Optional<UserRow> findByEmail(String email) {
        return dsl.select(
                        field("id", UUID.class),
                        field("email", String.class),
                        field("password_hash", String.class),
                        field("openai_api_key", String.class)
                )
                .from(table("users"))
                .where(field("email").eq(email.toLowerCase()))
                .fetchOptional(r -> new UserRow(
                        r.get(field("id", UUID.class)),
                        r.get(field("email", String.class)),
                        r.get(field("password_hash", String.class)),
                        r.get(field("openai_api_key", String.class))
                ));
    }

    @Override
    public boolean existsByEmail(String email) {
        return dsl.fetchCount(table("users"), field("email").eq(email.toLowerCase())) > 0;
    }

    @Override
    public UserRow save(UserRow user) {
        var id = user.id() != null ? user.id() : UUID.randomUUID();
        dsl.insertInto(table("users"))
                .columns(
                        field("id"), field("email"),
                        field("password_hash"), field("openai_api_key")
                )
                .values(id, user.email().toLowerCase(), user.passwordHash(), user.openaiApiKey())
                .onConflict(field("id"))
                .doUpdate()
                .set(field("email"), user.email().toLowerCase())
                .set(field("password_hash"), user.passwordHash())
                .set(field("updated_at"), now())
                .execute();
        return new UserRow(id, user.email().toLowerCase(), user.passwordHash(), user.openaiApiKey());
    }
}
