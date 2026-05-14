package com.sutulovai.jobops.repository.jooq;

import com.sutulovai.jobops.repository.ProfileRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class JooqProfileRepository implements ProfileRepository {

    private final DSLContext dsl;

    public JooqProfileRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<ProfileRow> findByUserId(UUID userId) {
        return dsl.select(asterisk())
                .from(table("user_profiles"))
                .where(field("user_id").eq(userId))
                .fetchOptional(this::toRow);
    }

    @Override
    public ProfileRow upsert(ProfileRow p) {
        var id = p.id() != null ? p.id() : UUID.randomUUID();
        dsl.insertInto(table("user_profiles"))
                .set(field("id"), id)
                .set(field("user_id"), p.userId())
                .set(field("full_name"), p.fullName())
                .set(field("current_location"), p.currentLocation())
                .set(field("target_countries"), toArray(p.targetCountries()))
                .set(field("target_cities"), toArray(p.targetCities()))
                .set(field("backup_cities"), toArray(p.backupCities()))
                .set(field("target_role_titles"), toArray(p.targetRoleTitles()))
                .set(field("target_salary_min"), p.targetSalaryMin())
                .set(field("target_salary_max"), p.targetSalaryMax())
                .set(field("minimum_salary"), p.minimumSalary())
                .set(field("salary_stretch_max"), p.salaryStretchMax())
                .set(field("availability"), p.availability())
                .set(field("relocation_status"), p.relocationStatus())
                .set(field("visa_readiness"), p.visaReadiness())
                .set(field("english_level"), p.englishLevel())
                .set(field("german_level"), p.germanLevel())
                .set(field("preferred_industries"), toArray(p.preferredIndustries()))
                .set(field("rejected_industries"), toArray(p.rejectedIndustries()))
                .set(field("preferred_company_types"), toArray(p.preferredCompanyTypes()))
                .set(field("rejected_company_types"), toArray(p.rejectedCompanyTypes()))
                .set(field("seniority_target"), p.seniorityTarget())
                .set(field("positioning_summary"), p.positioningSummary())
                .set(field("outreach_tone"), p.outreachTone())
                .set(field("timezone"), p.timezone() != null ? p.timezone() : "Europe/Berlin")
                .set(field("search_start_date"), p.searchStartDate())
                .set(field("updated_at"), now())
                .onConflict(field("user_id"))
                .doUpdate()
                .set(field("full_name"), p.fullName())
                .set(field("current_location"), p.currentLocation())
                .set(field("target_countries"), toArray(p.targetCountries()))
                .set(field("target_cities"), toArray(p.targetCities()))
                .set(field("backup_cities"), toArray(p.backupCities()))
                .set(field("target_role_titles"), toArray(p.targetRoleTitles()))
                .set(field("target_salary_min"), p.targetSalaryMin())
                .set(field("target_salary_max"), p.targetSalaryMax())
                .set(field("minimum_salary"), p.minimumSalary())
                .set(field("salary_stretch_max"), p.salaryStretchMax())
                .set(field("availability"), p.availability())
                .set(field("relocation_status"), p.relocationStatus())
                .set(field("visa_readiness"), p.visaReadiness())
                .set(field("english_level"), p.englishLevel())
                .set(field("german_level"), p.germanLevel())
                .set(field("preferred_industries"), toArray(p.preferredIndustries()))
                .set(field("rejected_industries"), toArray(p.rejectedIndustries()))
                .set(field("preferred_company_types"), toArray(p.preferredCompanyTypes()))
                .set(field("rejected_company_types"), toArray(p.rejectedCompanyTypes()))
                .set(field("seniority_target"), p.seniorityTarget())
                .set(field("positioning_summary"), p.positioningSummary())
                .set(field("outreach_tone"), p.outreachTone())
                .set(field("timezone"), p.timezone() != null ? p.timezone() : "Europe/Berlin")
                .set(field("search_start_date"), p.searchStartDate())
                .set(field("updated_at"), now())
                .execute();
        return p.id() == null ? new ProfileRow(id, p.userId(), p.fullName(), p.currentLocation(),
                p.targetCountries(), p.targetCities(), p.backupCities(), p.targetRoleTitles(),
                p.targetSalaryMin(), p.targetSalaryMax(), p.minimumSalary(), p.salaryStretchMax(),
                p.availability(), p.relocationStatus(), p.visaReadiness(), p.englishLevel(),
                p.germanLevel(), p.preferredIndustries(), p.rejectedIndustries(),
                p.preferredCompanyTypes(), p.rejectedCompanyTypes(), p.seniorityTarget(),
                p.positioningSummary(), p.outreachTone(), p.timezone(), p.searchStartDate()) : p;
    }

    private ProfileRow toRow(org.jooq.Record r) {
        return new ProfileRow(
                r.get(field("id", UUID.class)),
                r.get(field("user_id", UUID.class)),
                r.get(field("full_name", String.class)),
                r.get(field("current_location", String.class)),
                toList(r.get(field("target_countries", String[].class))),
                toList(r.get(field("target_cities", String[].class))),
                toList(r.get(field("backup_cities", String[].class))),
                toList(r.get(field("target_role_titles", String[].class))),
                r.get(field("target_salary_min", Integer.class)),
                r.get(field("target_salary_max", Integer.class)),
                r.get(field("minimum_salary", Integer.class)),
                r.get(field("salary_stretch_max", Integer.class)),
                r.get(field("availability", String.class)),
                r.get(field("relocation_status", String.class)),
                r.get(field("visa_readiness", String.class)),
                r.get(field("english_level", String.class)),
                r.get(field("german_level", String.class)),
                toList(r.get(field("preferred_industries", String[].class))),
                toList(r.get(field("rejected_industries", String[].class))),
                toList(r.get(field("preferred_company_types", String[].class))),
                toList(r.get(field("rejected_company_types", String[].class))),
                r.get(field("seniority_target", String.class)),
                r.get(field("positioning_summary", String.class)),
                r.get(field("outreach_tone", String.class)),
                r.get(field("timezone", String.class)),
                r.get(field("search_start_date", LocalDate.class))
        );
    }

    private static String[] toArray(List<String> list) {
        return list == null ? new String[0] : list.toArray(new String[0]);
    }

    private static List<String> toList(String[] arr) {
        return arr == null ? List.of() : Arrays.asList(arr);
    }
}
