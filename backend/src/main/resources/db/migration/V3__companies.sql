create table companies (
    id                   uuid        not null default gen_random_uuid() primary key,
    user_id              uuid        not null references users (id) on delete cascade,
    name                 text        not null,
    website              text,
    careers_page_url     text,
    linked_in_url        text,
    city                 text,
    country              text,
    office_locations     text[]      not null default '{}',
    remote_policy        text,
    industry             text,
    company_size         text,
    funding_status       text,
    priority_tier        text        not null default 'P2',
    english_likelihood   text        not null default 'UNCERTAIN',
    relocation_friendly  text        not null default 'UNCERTAIN',
    visa_sponsorship     text        not null default 'UNCERTAIN',
    salary_pitch_min     int,
    salary_pitch_max     int,
    company_type         text,
    fit_reason           text,
    likely_roles         text[]      not null default '{}',
    recommended_strategy text,
    notes                text,
    source_url           text,
    status               text        not null default 'WATCHLIST',
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now(),
    unique (user_id, name)
);

create index idx_companies_user_id on companies (user_id);
create index idx_companies_user_status on companies (user_id, status);
create index idx_companies_user_tier on companies (user_id, priority_tier);
