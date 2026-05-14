create table applications (
    id                        uuid        not null default gen_random_uuid() primary key,
    user_id                   uuid        not null references users (id) on delete cascade,
    vacancy_id                uuid        not null references vacancies (id) on delete cascade,
    company_id                uuid        references companies (id) on delete set null,
    cv_id                     uuid        references cvs (id) on delete set null,
    stage                     text        not null default 'ADDED_TO_PIPELINE',
    application_channel       text,
    source_channel            text,
    date_applied              date,
    recruiter_contacted       boolean     not null default false,
    hiring_manager_contacted  boolean     not null default false,
    referral_requested        boolean     not null default false,
    follow_up_count           int         not null default 0,
    last_contact_date         date,
    next_action_date          date,
    priority                  int         not null default 50,
    stale                     boolean     not null default false,
    notes                     text,
    outcome                   text,
    rejection_reason          text,
    city_category             text,
    created_at                timestamptz not null default now(),
    updated_at                timestamptz not null default now()
);

create index idx_applications_user_id on applications (user_id);
create index idx_applications_user_stage on applications (user_id, stage);
create index idx_applications_vacancy on applications (vacancy_id);
create index idx_applications_company on applications (company_id);
create index idx_applications_stale on applications (user_id, stale);

create table contacts (
    id                   uuid        not null default gen_random_uuid() primary key,
    user_id              uuid        not null references users (id) on delete cascade,
    company_id           uuid        references companies (id) on delete set null,
    name                 text        not null,
    title                text,
    contact_type         text        not null default 'OTHER',
    linked_in_url        text,
    email                text,
    relationship_strength text       not null default 'COLD',
    source               text,
    last_contacted_date  date,
    next_follow_up_date  date,
    notes                text,
    preferred_channel    text        not null default 'LINKEDIN',
    status               text        not null default 'NEW',
    vacancy_id           uuid        references vacancies (id) on delete set null,
    application_id       uuid        references applications (id) on delete set null,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);

create index idx_contacts_user_id on contacts (user_id);
create index idx_contacts_company on contacts (company_id);
