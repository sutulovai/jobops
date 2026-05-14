package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.SavedSearchRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqSavedSearchRepository implements SavedSearchRepository {

    private final DSLContext dsl;

    public JooqSavedSearchRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<SearchRow> findByUserId(UUID userId) {
        return dsl.selectFrom(table("saved_searches"))
                .where(field("user_id").eq(userId))
                .orderBy(field("title").asc())
                .fetch(this::toRow);
    }

    @Override
    public Optional<SearchRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("saved_searches"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public SearchRow save(SearchRow s) {
        var id = s.id() != null ? s.id() : UUID.randomUUID();
        dsl.insertInto(table("saved_searches"))
                .set(field("id"), id)
                .set(field("user_id"), s.userId())
                .set(field("title"), s.title())
                .set(field("platform"), s.platform() != null ? s.platform() : "LINKEDIN")
                .set(field("url"), s.url())
                .set(field("query_text"), s.queryText())
                .set(field("boolean_query"), s.booleanQuery())
                .set(field("city"), s.city())
                .set(field("keywords"), s.keywords() != null ? s.keywords() : new String[0])
                .set(field("frequency"), s.frequency() != null ? s.frequency() : "WEEKLY")
                .set(field("last_checked_date"), s.lastCheckedDate())
                .set(field("next_check_date"), s.nextCheckDate())
                .set(field("useful"), s.useful())
                .set(field("yield_rating"), s.yieldRating() != null ? s.yieldRating() : "UNKNOWN")
                .set(field("jobs_added_count"), s.jobsAddedCount())
                .set(field("applications_created_count"), s.applicationsCreatedCount())
                .set(field("responses_from_count"), s.responsesFromCount())
                .set(field("notes"), s.notes())
                .set(field("active"), s.active())
                .onConflict(field("id"))
                .doUpdate()
                .set(field("title"), s.title())
                .set(field("platform"), s.platform() != null ? s.platform() : "LINKEDIN")
                .set(field("url"), s.url())
                .set(field("query_text"), s.queryText())
                .set(field("boolean_query"), s.booleanQuery())
                .set(field("city"), s.city())
                .set(field("keywords"), s.keywords() != null ? s.keywords() : new String[0])
                .set(field("frequency"), s.frequency() != null ? s.frequency() : "WEEKLY")
                .set(field("last_checked_date"), s.lastCheckedDate())
                .set(field("next_check_date"), s.nextCheckDate())
                .set(field("useful"), s.useful())
                .set(field("yield_rating"), s.yieldRating() != null ? s.yieldRating() : "UNKNOWN")
                .set(field("notes"), s.notes())
                .set(field("active"), s.active())
                .set(field("updated_at"), now())
                .execute();
        return findById(id, s.userId()).orElseThrow();
    }

    @Override
    public void markChecked(UUID id, UUID userId, String nextCheckDate) {
        dsl.update(table("saved_searches"))
                .set(field("last_checked_date"), LocalDate.now().toString())
                .set(field("next_check_date"), nextCheckDate)
                .set(field("updated_at"), now())
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    @Override
    public void delete(UUID id, UUID userId) {
        dsl.deleteFrom(table("saved_searches"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    private SearchRow toRow(Record r) {
        return new SearchRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("title", String.class)),
                r.get(field("platform", String.class)),
                r.get(field("url", String.class)),
                r.get(field("query_text", String.class)),
                r.get(field("boolean_query", String.class)),
                r.get(field("city", String.class)),
                r.get(field("keywords", String[].class)),
                r.get(field("frequency", String.class)),
                optStr(r, "last_checked_date"),
                optStr(r, "next_check_date"),
                r.get(field("useful", Boolean.class)),
                r.get(field("yield_rating", String.class)),
                r.get(field("jobs_added_count", Integer.class)),
                r.get(field("applications_created_count", Integer.class)),
                r.get(field("responses_from_count", Integer.class)),
                r.get(field("notes", String.class)),
                r.get(field("active", Boolean.class)),
                RecordTimes.instantUtcOrNow(r, "created_at"),
                RecordTimes.instantUtcOrNow(r, "updated_at")
        );
    }

    private static String optStr(Record r, String f) {
        var v = r.get(field(f));
        return v != null ? v.toString() : null;
    }
}
