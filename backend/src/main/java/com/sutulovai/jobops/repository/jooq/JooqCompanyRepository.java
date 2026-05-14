package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.CompanyRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqCompanyRepository implements CompanyRepository {

    private final DSLContext dsl;

    public JooqCompanyRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<CompanyRow> findByUserId(UUID userId) {
        return dsl.selectFrom(table("companies"))
                .where(field("user_id").eq(userId))
                .orderBy(
                        field("priority_tier").asc(),
                        field("name").asc()
                )
                .fetch(this::toRow);
    }

    @Override
    public Optional<CompanyRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(table("companies"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .fetchOptional(this::toRow);
    }

    @Override
    public Optional<CompanyRow> findByName(String name, UUID userId) {
        return dsl.selectFrom(table("companies"))
                .where(field("user_id").eq(userId)
                        .and(lower(field("name", String.class)).eq(name.toLowerCase())))
                .fetchOptional(this::toRow);
    }

    @Override
    public CompanyRow save(CompanyRow c) {
        var id = c.id() != null ? c.id() : UUID.randomUUID();
        dsl.insertInto(table("companies"))
                .set(field("id"), id)
                .set(field("user_id"), c.userId())
                .set(field("name"), c.name())
                .set(field("website"), c.website())
                .set(field("careers_page_url"), c.careersPageUrl())
                .set(field("linked_in_url"), c.linkedInUrl())
                .set(field("city"), c.city())
                .set(field("country"), c.country())
                .set(field("office_locations"), c.officeLocations() != null ? c.officeLocations() : new String[0])
                .set(field("remote_policy"), c.remotePolicy())
                .set(field("industry"), c.industry())
                .set(field("company_size"), c.companySize())
                .set(field("funding_status"), c.fundingStatus())
                .set(field("priority_tier"), c.priorityTier() != null ? c.priorityTier() : "P2")
                .set(field("english_likelihood"), c.englishLikelihood() != null ? c.englishLikelihood() : "UNCERTAIN")
                .set(field("relocation_friendly"), c.relocationFriendly() != null ? c.relocationFriendly() : "UNCERTAIN")
                .set(field("visa_sponsorship"), c.visaSponsorship() != null ? c.visaSponsorship() : "UNCERTAIN")
                .set(field("salary_pitch_min"), c.salaryPitchMin())
                .set(field("salary_pitch_max"), c.salaryPitchMax())
                .set(field("company_type"), c.companyType())
                .set(field("fit_reason"), c.fitReason())
                .set(field("likely_roles"), c.likelyRoles() != null ? c.likelyRoles() : new String[0])
                .set(field("recommended_strategy"), c.recommendedStrategy())
                .set(field("notes"), c.notes())
                .set(field("source_url"), c.sourceUrl())
                .set(field("status"), c.status() != null ? c.status() : "WATCHLIST")
                .onConflict(field("user_id"), field("name"))
                .doUpdate()
                .set(field("website"), c.website())
                .set(field("careers_page_url"), c.careersPageUrl())
                .set(field("linked_in_url"), c.linkedInUrl())
                .set(field("city"), c.city())
                .set(field("country"), c.country())
                .set(field("office_locations"), c.officeLocations() != null ? c.officeLocations() : new String[0])
                .set(field("remote_policy"), c.remotePolicy())
                .set(field("industry"), c.industry())
                .set(field("company_size"), c.companySize())
                .set(field("funding_status"), c.fundingStatus())
                .set(field("priority_tier"), c.priorityTier() != null ? c.priorityTier() : "P2")
                .set(field("english_likelihood"), c.englishLikelihood() != null ? c.englishLikelihood() : "UNCERTAIN")
                .set(field("relocation_friendly"), c.relocationFriendly() != null ? c.relocationFriendly() : "UNCERTAIN")
                .set(field("visa_sponsorship"), c.visaSponsorship() != null ? c.visaSponsorship() : "UNCERTAIN")
                .set(field("salary_pitch_min"), c.salaryPitchMin())
                .set(field("salary_pitch_max"), c.salaryPitchMax())
                .set(field("company_type"), c.companyType())
                .set(field("fit_reason"), c.fitReason())
                .set(field("likely_roles"), c.likelyRoles() != null ? c.likelyRoles() : new String[0])
                .set(field("recommended_strategy"), c.recommendedStrategy())
                .set(field("notes"), c.notes())
                .set(field("source_url"), c.sourceUrl())
                .set(field("status"), c.status() != null ? c.status() : "WATCHLIST")
                .set(field("updated_at"), now())
                .execute();
        return c.id() == null ? findByName(c.name(), c.userId()).orElseThrow() : c;
    }

    @Override
    public void delete(UUID id, UUID userId) {
        dsl.deleteFrom(table("companies"))
                .where(field("id").eq(id).and(field("user_id").eq(userId)))
                .execute();
    }

    private CompanyRow toRow(Record r) {
        return new CompanyRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("name", String.class)),
                r.get(field("website", String.class)),
                r.get(field("careers_page_url", String.class)),
                r.get(field("linked_in_url", String.class)),
                r.get(field("city", String.class)),
                r.get(field("country", String.class)),
                r.get(field("office_locations", String[].class)),
                r.get(field("remote_policy", String.class)),
                r.get(field("industry", String.class)),
                r.get(field("company_size", String.class)),
                r.get(field("funding_status", String.class)),
                r.get(field("priority_tier", String.class)),
                r.get(field("english_likelihood", String.class)),
                r.get(field("relocation_friendly", String.class)),
                r.get(field("visa_sponsorship", String.class)),
                r.get(field("salary_pitch_min", Integer.class)),
                r.get(field("salary_pitch_max", Integer.class)),
                r.get(field("company_type", String.class)),
                r.get(field("fit_reason", String.class)),
                r.get(field("likely_roles", String[].class)),
                r.get(field("recommended_strategy", String.class)),
                r.get(field("notes", String.class)),
                r.get(field("source_url", String.class)),
                r.get(field("status", String.class)),
                RecordTimes.instantUtcOrNow(r, "created_at"),
                RecordTimes.instantUtcOrNow(r, "updated_at")
        );
    }
}
