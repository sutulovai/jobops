package com.sutulovai.jobops.service;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.jooq.impl.DSL.*;

/**
 * Seeds target companies and saved searches for a new user.
 * Called once per user on first registration.
 */
@Service
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DSLContext dsl;

    public DataSeeder(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional
    public void seedForUser(UUID userId) {
        log.info("🔵 Seeding default data for user {}", userId);
        seedCompanies(userId);
        seedSavedSearches(userId);
        log.info("✅ Seeding complete for user {}", userId);
    }

    private void seedCompanies(UUID userId) {
        var companies = new Object[][]{
            // P1
            {"Scalable Capital", "Munich", "Germany", "P1", "FINTECH", "YES", "UNCERTAIN",
             "Regulated fintech, Kotlin/Java, correctness, wealth management infrastructure",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "RECRUITER_MESSAGE", 85000, 105000,
             "Top fintech/investing platform in Munich. Kotlin/Java stack. Highly relevant for payments/fintech positioning."},
            {"SumUp", "Munich", "Germany", "P1", "FINTECH", "YES", "UNCERTAIN",
             "Payments, acquiring, merchant systems, transaction flows",
             new String[]{"Senior Backend Engineer", "Payments Engineer"},
             "RECRUITER_MESSAGE", 85000, 105000,
             "Major payments company with Munich office. Excellent fit for payments/acquiring background."},
            {"Flix", "Munich", "Germany", "P1", "MOBILITY", "YES", "UNCERTAIN",
             "Java/Kotlin backend, platform, high-scale distributed systems",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 100000,
             "Strong Munich tech company. Java backend, platform engineering, distributed systems."},
            {"Celonis", "Munich", "Germany", "P1", "B2B_SAAS", "YES", "UNCERTAIN",
             "Strong Munich product company, scale, B2B SaaS platform",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "REFERRAL_REQUEST", 85000, 105000,
             "Top Munich B2B SaaS company. Strong engineering culture, international team."},
            {"Holidu", "Munich", "Germany", "P1", "ECOMMERCE", "YES", "UNCERTAIN",
             "Backend/e-commerce fit, bookings platform, distributed systems",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "DIRECT_APPLY", 80000, 100000,
             "Munich travel/e-commerce platform. Java/Kotlin backend."},
            {"Personio", "Munich", "Germany", "P1", "B2B_SAAS", "YES", "UNCERTAIN",
             "Strong international product engineering, B2B SaaS, Munich HQ",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "REFERRAL_REQUEST", 85000, 100000,
             "Leading Munich HR SaaS. International team, strong engineering culture."},
            {"FINN", "Munich", "Germany", "P1", "MOBILITY", "YES", "UNCERTAIN",
             "Mobility/product/backend ownership, modern backend stack",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "DIRECT_APPLY", 80000, 100000,
             "Munich mobility startup. Backend ownership, product engineering."},
            {"NavVis", "Munich", "Germany", "P1", "PRODUCT", "YES", "UNCERTAIN",
             "Cloud/platform/distributed systems, engineering depth",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 95000,
             "Munich deep-tech platform company. Distributed systems."},
            {"Apaleo", "Munich", "Germany", "P1", "B2B_SAAS", "YES", "UNCERTAIN",
             "API-first SaaS platform, hospitality tech, strong backend",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "DIRECT_APPLY", 75000, 95000,
             "Munich/Berlin API-first SaaS. Strong backend ownership."},
            {"EGYM", "Munich", "Germany", "P1", "PRODUCT", "YES", "UNCERTAIN",
             "Product/platform engineering, Munich-based, international",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 100000,
             "Munich fitness/health platform. International team, strong engineering."},
            // P1.5
            {"N26", "Berlin", "Germany", "P1_5", "FINTECH", "YES", "UNCERTAIN",
             "Banking/fintech/payments/platform, regulated, distributed systems",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "RECRUITER_MESSAGE", 85000, 105000,
             "Leading Berlin fintech bank. Payments/platform engineering, distributed systems."},
            {"Trade Republic", "Berlin", "Germany", "P1_5", "FINTECH", "YES", "UNCERTAIN",
             "Trading/investments/regulated fintech, correctness-heavy backend",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "RECRUITER_MESSAGE", 85000, 110000,
             "Berlin trading/investment platform. Regulated fintech, high correctness requirements."},
            {"Billie", "Berlin", "Germany", "P1_5", "FINTECH", "YES", "UNCERTAIN",
             "B2B payments/BNPL/risk, fintech infrastructure",
             new String[]{"Senior Backend Engineer", "Payments Engineer"},
             "RECRUITER_MESSAGE", 80000, 105000,
             "Berlin B2B payments and BNPL. Strong fintech/payments fit."},
            {"Pliant", "Berlin", "Germany", "P1_5", "FINTECH", "YES", "UNCERTAIN",
             "B2B payments/cards/banking infrastructure",
             new String[]{"Senior Backend Engineer", "Payments Engineer"},
             "RECRUITER_MESSAGE", 80000, 100000,
             "Berlin B2B payments/cards company. Banking infrastructure fit."},
            {"Taxfix", "Berlin", "Germany", "P1_5", "FINTECH", "YES", "UNCERTAIN",
             "Regulated fintech, correctness-heavy backend, tax platform",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "DIRECT_APPLY", 80000, 100000,
             "Berlin regulated fintech. Correctness, compliance, backend ownership."},
            {"Contentful", "Berlin", "Germany", "P1_5", "B2B_SAAS", "YES", "UNCERTAIN",
             "API/platform infrastructure, distributed systems, scale",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000,
             "Berlin API-first SaaS platform. Strong distributed systems engineering."},
            {"GetYourGuide", "Berlin", "Germany", "P1_5", "ECOMMERCE", "YES", "UNCERTAIN",
             "Marketplace/e-commerce/travel backend, high scale",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "REFERRAL_REQUEST", 80000, 105000,
             "Berlin marketplace/travel platform. E-commerce checkout, high scale."},
            {"Delivery Hero", "Berlin", "Germany", "P1_5", "ECOMMERCE", "YES", "UNCERTAIN",
             "High-scale distributed systems, platform, e-commerce",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000,
             "Berlin high-scale food delivery. Distributed systems at scale."},
            {"ABOUT YOU", "Hamburg", "Germany", "P1_5", "ECOMMERCE", "YES", "UNCERTAIN",
             "E-commerce/platform/checkout, high scale, Hamburg",
             new String[]{"Senior Backend Engineer", "Senior Software Engineer"},
             "DIRECT_APPLY", 75000, 100000,
             "Hamburg e-commerce platform. Checkout/payments adjacent."},
            {"FREENOW", "Hamburg", "Germany", "P1_5", "MOBILITY", "YES", "UNCERTAIN",
             "Mobility/platform/payments-adjacent",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 75000, 100000,
             "Hamburg/Berlin mobility platform. Payments-adjacent, distributed systems."},
            // P2
            {"Mambu", "Berlin", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Banking platform SaaS, cloud-native banking infrastructure",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Berlin banking-as-a-service platform."},
            {"Solaris", "Berlin", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Banking-as-a-service, regulated fintech, infrastructure",
             new String[]{"Senior Backend Engineer", "Payments Engineer"},
             "RECRUITER_MESSAGE", 80000, 105000, "Berlin BaaS platform. Regulated fintech."},
            {"Moss", "Berlin", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "B2B fintech, spend management, payments",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Berlin B2B fintech/spend management."},
            {"Raisin", "Berlin", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Savings/investment platform, regulated fintech",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Berlin savings platform. Regulated fintech."},
            {"CLARK", "Frankfurt", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Insurtech/fintech, regulated, Frankfurt",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Frankfurt insurtech/fintech."},
            {"Deutsche Bank Technology", "Frankfurt", "Germany", "P2", "BANK", "UNCERTAIN", "UNCERTAIN",
             "Banking technology, financial systems, distributed systems",
             new String[]{"Senior Software Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Frankfurt banking technology. Large-scale financial systems."},
            {"ING Deutschland", "Frankfurt", "Germany", "P2", "BANK", "YES", "UNCERTAIN",
             "Banking platform, payments, distributed systems",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 100000, "Frankfurt digital bank. English-speaking teams."},
            {"C24 Bank", "Frankfurt", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Digital bank, fintech, payments",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Frankfurt digital bank. Fintech/payments."},
            {"CHECK24", "Munich", "Germany", "P2", "ECOMMERCE", "UNCERTAIN", "UNCERTAIN",
             "Insurance/e-commerce platform, high scale, Munich",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 95000, "Munich comparison platform. German-heavy culture."},
            {"PAYBACK", "Munich", "Germany", "P2", "ECOMMERCE", "UNCERTAIN", "UNCERTAIN",
             "Loyalty/payments platform, Munich",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 90000, "Munich loyalty/payments. German culture likely."},
            {"Giesecke+Devrient", "Munich", "Germany", "P2", "FINTECH", "UNCERTAIN", "UNCERTAIN",
             "Payment security, banking technology, Munich",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Munich payment security company."},
            {"AutoScout24", "Munich", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "Marketplace platform, high scale, Munich",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Munich automotive marketplace."},
            {"HelloFresh", "Berlin", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "E-commerce/subscriptions/platform, high scale, Berlin",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Berlin e-commerce/subscriptions platform."},
            {"adjoe", "Hamburg", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "AdTech/platform, Hamburg, distributed systems",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Hamburg AdTech platform."},
            {"applike group", "Hamburg", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "AdTech/platform, Hamburg, high scale",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 95000, "Hamburg AdTech/gaming platform."},
            {"SAP", "Munich", "Germany", "P2", "ENTERPRISE", "YES", "YES",
             "Enterprise platform, cloud, distributed systems, Munich",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Munich enterprise cloud platform. Known relocation support."},
            {"SAP LeanIX", "Munich", "Germany", "P2", "B2B_SAAS", "YES", "UNCERTAIN",
             "B2B SaaS/platform, Munich, enterprise",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 80000, 100000, "Munich B2B SaaS (SAP subsidiary)."},
            {"Staffbase", "Berlin", "Germany", "P2", "B2B_SAAS", "YES", "UNCERTAIN",
             "B2B SaaS, employee comms platform, Berlin",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Berlin B2B SaaS. International team."},
            {"JetBrains", "Munich", "Germany", "P2", "PRODUCT", "YES", "UNCERTAIN",
             "Developer tools/platform, Munich, strong engineering",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Munich developer tools company. International team."},
            {"Porsche Digital", "Stuttgart", "Germany", "P2", "MOBILITY", "YES", "UNCERTAIN",
             "Mobility/digital platform, Stuttgart, Porsche brand",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Stuttgart digital/mobility platform."},
            {"DATEV", "Nuremberg", "Germany", "P2", "FINTECH", "UNCERTAIN", "UNCERTAIN",
             "Financial software, Nuremberg, German-heavy",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 90000, "Nuremberg financial software. German culture likely."},
            {"Applied Intuition", "Munich", "Germany", "P2", "PRODUCT", "YES", "YES",
             "Autonomous systems platform, Munich, US company",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 85000, 115000, "Munich autonomous systems. US company, relocation support likely."},
            {"Helsing", "Munich", "Germany", "P2", "PRODUCT", "YES", "UNCERTAIN",
             "Defence AI platform, Munich, distributed systems",
             new String[]{"Senior Backend Engineer", "Platform Engineer"},
             "DIRECT_APPLY", 85000, 115000, "Munich AI/defence platform. Cutting-edge engineering."},
            {"Isar Aerospace", "Munich", "Germany", "P2", "PRODUCT", "YES", "UNCERTAIN",
             "Space/rockets platform, Munich, high-scale systems",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 80000, 105000, "Munich aerospace startup."},
            {"Quantum Systems", "Munich", "Germany", "P2", "PRODUCT", "YES", "UNCERTAIN",
             "Drone systems platform, Munich",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Munich drone systems company."},
            {"Emma", "Frankfurt", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "E-commerce/D2C, Frankfurt, distributed systems",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 100000, "Frankfurt D2C e-commerce platform."},
            {"ottonova", "Munich", "Germany", "P2", "FINTECH", "YES", "UNCERTAIN",
             "Health insurance/fintech, Munich, regulated",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 75000, 95000, "Munich health insurance fintech."},
            {"zooplus", "Munich", "Germany", "P2", "ECOMMERCE", "YES", "UNCERTAIN",
             "E-commerce platform, Munich, distributed systems",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 95000, "Munich pet e-commerce platform."},
            {"Freeletics", "Munich", "Germany", "P2", "PRODUCT", "YES", "UNCERTAIN",
             "Consumer tech platform, Munich",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 90000, "Munich health/fitness platform."},
            {"Statista", "Hamburg", "Germany", "P2", "B2B_SAAS", "YES", "UNCERTAIN",
             "Data/analytics SaaS platform, Hamburg",
             new String[]{"Senior Backend Engineer"},
             "DIRECT_APPLY", 70000, 90000, "Hamburg data/analytics SaaS platform."},
        };

        for (var c : companies) {
            var companyId = UUID.randomUUID();
            try {
                dsl.insertInto(table("companies"))
                        .set(field("id"), companyId)
                        .set(field("user_id"), userId)
                        .set(field("name"), c[0])
                        .set(field("city"), c[1])
                        .set(field("country"), c[2])
                        .set(field("priority_tier"), c[3])
                        .set(field("company_type"), c[4])
                        .set(field("english_likelihood"), c[5])
                        .set(field("relocation_friendly"), c[6])
                        .set(field("fit_reason"), c[7])
                        .set(field("likely_roles"), (String[]) c[8])
                        .set(field("recommended_strategy"), c[9])
                        .set(field("salary_pitch_min"), c[10])
                        .set(field("salary_pitch_max"), c[11])
                        .set(field("notes"), c[12])
                        .set(field("status"), "WATCHLIST")
                        .onConflict(field("user_id"), field("name"))
                        .doNothing()
                        .execute();
            } catch (Exception e) {
                log.warn("⚠️ Could not seed company {}: {}", c[0], e.getMessage());
            }
        }
    }

    private void seedSavedSearches(UUID userId) {
        var searches = new Object[][]{
            {"Senior Backend Java/Kotlin Munich", "LINKEDIN",
             "Senior Backend Engineer Java Kotlin Munich",
             "Munich", "WEEKLY"},
            {"Senior Backend Java/Kotlin Berlin", "LINKEDIN",
             "Senior Backend Engineer Java Kotlin Berlin",
             "Berlin", "WEEKLY"},
            {"Payments Backend Germany", "LINKEDIN",
             "Payments Backend Engineer Germany",
             "Germany", "WEEKLY"},
            {"Fintech Backend Germany", "LINKEDIN",
             "Fintech Backend Engineer Germany",
             "Germany", "WEEKLY"},
            {"Backend Platform Germany Java", "LINKEDIN",
             "Backend Platform Engineer Germany Java",
             "Germany", "WEEKLY"},
            {"SRE Platform Fintech Germany", "LINKEDIN",
             "Site Reliability Engineer Platform Germany fintech",
             "Germany", "WEEKLY"},
            {"Recruiters Backend Munich", "LINKEDIN",
             "Talent Acquisition Backend Munich Germany",
             "Munich", "WEEKLY"},
            {"EM Payments Berlin", "LINKEDIN",
             "Engineering Manager Payments Berlin Germany",
             "Berlin", "WEEKLY"},
            {"Java Kotlin English Germany", "LINKEDIN",
             "Java Kotlin Spring Boot Germany English speaking",
             "Germany", "WEEKLY"},
            {"Visa Backend Germany", "LINKEDIN",
             "Visa sponsorship backend engineer Germany",
             "Germany", "WEEKLY"},
            {"GermanTechJobs Backend", "GERMANTECHJOBS",
             "Senior Backend Engineer Java Kotlin",
             "Germany", "WEEKLY"},
            {"Arbeitnow Visa Backend", "ARBEITNOW",
             "Backend Engineer visa sponsorship Java",
             "Germany", "WEEKLY"},
            {"Relocate.me Backend Germany", "RELOCATE_ME",
             "Senior Backend Engineer Germany",
             "Germany", "WEEKLY"},
        };

        for (var s : searches) {
            try {
                dsl.insertInto(table("saved_searches"))
                        .set(field("id"), UUID.randomUUID())
                        .set(field("user_id"), userId)
                        .set(field("title"), s[0])
                        .set(field("platform"), s[1])
                        .set(field("query_text"), s[2])
                        .set(field("city"), s[3])
                        .set(field("frequency"), s[4])
                        .set(field("active"), true)
                        .execute();
            } catch (Exception e) {
                log.warn("⚠️ Could not seed search {}: {}", s[0], e.getMessage());
            }
        }
    }
}
