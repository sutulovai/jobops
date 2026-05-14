create extension if not exists "pgcrypto";

create table users (
    id            uuid        not null default gen_random_uuid() primary key,
    email         text        not null unique,
    password_hash text        not null,
    openai_api_key text,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

create index idx_users_email on users (email);

create table user_profiles (
    id                      uuid        not null default gen_random_uuid() primary key,
    user_id                 uuid        not null unique references users (id) on delete cascade,
    full_name               text,
    current_location        text,
    target_countries        text[]      not null default '{}',
    target_cities           text[]      not null default '{}',
    backup_cities           text[]      not null default '{}',
    target_role_titles      text[]      not null default '{}',
    target_salary_min       int,
    target_salary_max       int,
    minimum_salary          int,
    salary_stretch_max      int,
    availability            text,
    relocation_status       text,
    visa_readiness          text,
    english_level           text,
    german_level            text,
    preferred_industries    text[]      not null default '{}',
    rejected_industries     text[]      not null default '{}',
    preferred_company_types text[]      not null default '{}',
    rejected_company_types  text[]      not null default '{}',
    seniority_target        text,
    positioning_summary     text,
    outreach_tone           text,
    timezone                text        not null default 'Europe/Berlin',
    search_start_date       date,
    created_at              timestamptz not null default now(),
    updated_at              timestamptz not null default now()
);
