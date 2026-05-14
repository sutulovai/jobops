package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.CvRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqCvRepository implements CvRepository {

    private final DSLContext dsl;

    public JooqCvRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<CvRow> findByUserId(UUID userId) {
        return dsl.select(
                        field("id", UUID.class),
                        field("user_id", UUID.class),
                        field("label", String.class),
                        field("version", Integer.class),
                        field("is_default", Boolean.class),
                        field("original_filename", String.class),
                        field("storage_path", String.class),
                        field("mime_type", String.class),
                        field("file_size_bytes", Long.class),
                        field("created_at"),
                        field("updated_at")
                )
                .from(table("cvs"))
                .where(field("user_id").eq(userId))
                .orderBy(field("created_at").desc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<CvRow> findById(UUID id) {
        return dsl.select(
                        field("id", UUID.class),
                        field("user_id", UUID.class),
                        field("label", String.class),
                        field("version", Integer.class),
                        field("is_default", Boolean.class),
                        field("original_filename", String.class),
                        field("storage_path", String.class),
                        field("mime_type", String.class),
                        field("file_size_bytes", Long.class),
                        field("created_at"),
                        field("updated_at")
                )
                .from(table("cvs"))
                .where(field("id").eq(id))
                .fetchOptional(this::toRow);
    }

    @Override
    public Optional<CvRow> findDefaultByUserId(UUID userId) {
        return dsl.select(
                        field("id", UUID.class),
                        field("user_id", UUID.class),
                        field("label", String.class),
                        field("version", Integer.class),
                        field("is_default", Boolean.class),
                        field("original_filename", String.class),
                        field("storage_path", String.class),
                        field("mime_type", String.class),
                        field("file_size_bytes", Long.class),
                        field("created_at"),
                        field("updated_at")
                )
                .from(table("cvs"))
                .where(field("user_id").eq(userId).and(field("is_default").eq(true)))
                .fetchOptional(this::toRow);
    }

    @Override
    public CvRow save(CvRow cv) {
        var id = cv.id() != null ? cv.id() : UUID.randomUUID();
        dsl.insertInto(table("cvs"))
                .set(field("id"), id)
                .set(field("user_id"), cv.userId())
                .set(field("label"), cv.label())
                .set(field("version"), cv.version())
                .set(field("is_default"), cv.isDefault())
                .set(field("original_filename"), cv.originalFilename())
                .set(field("storage_path"), cv.storagePath())
                .set(field("mime_type"), cv.mimeType())
                .set(field("file_size_bytes"), cv.fileSizeBytes())
                .execute();
        return new CvRow(id, cv.userId(), cv.label(), cv.version(), cv.isDefault(),
                cv.originalFilename(), cv.storagePath(), cv.mimeType(), cv.fileSizeBytes(),
                Instant.now(), Instant.now());
    }

    @Override
    public void clearDefaultForUser(UUID userId) {
        dsl.update(table("cvs"))
                .set(field("is_default"), false)
                .set(field("updated_at"), now())
                .where(field("user_id").eq(userId))
                .execute();
    }

    @Override
    public void setDefault(UUID id, UUID userId) {
        dsl.update(table("cvs"))
                .set(field("is_default"), true)
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void delete(UUID id, UUID userId) {
        dsl.deleteFrom(table("cvs"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public String findExtractedText(UUID id) {
        return dsl.select(field("extracted_text", String.class))
                .from(table("cvs"))
                .where(field("id").eq(id))
                .fetchOne(field("extracted_text", String.class));
    }

    public void updateExtractedText(UUID id, String text) {
        dsl.update(table("cvs"))
                .set(field("extracted_text"), text)
                .set(field("updated_at"), now())
                .where(field("id").eq(id))
                .execute();
    }

    private CvRow toRow(org.jooq.Record r) {
        var createdAt = RecordTimes.instantUtcOrNow(r, "created_at");
        var updatedAt = RecordTimes.instantUtcOrNow(r, "updated_at");
        return new CvRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("label", String.class)),
                r.get(field("version", Integer.class)),
                r.get(field("is_default", Boolean.class)),
                r.get(field("original_filename", String.class)),
                r.get(field("storage_path", String.class)),
                r.get(field("mime_type", String.class)),
                r.get(field("file_size_bytes", Long.class)),
                createdAt,
                updatedAt
        );
    }
}
