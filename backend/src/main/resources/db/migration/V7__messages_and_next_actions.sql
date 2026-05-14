create table outreach_messages (
    id                  uuid        not null default gen_random_uuid() primary key,
    user_id             uuid        not null references users (id) on delete cascade,
    contact_id          uuid        references contacts (id) on delete set null,
    company_id          uuid        references companies (id) on delete set null,
    vacancy_id          uuid        references vacancies (id) on delete set null,
    application_id      uuid        references applications (id) on delete set null,
    message_type        text        not null,
    channel             text        not null default 'LINKEDIN',
    recipient_type      text,
    generated_text      text        not null,
    edited_final_text   text,
    status              text        not null default 'DRAFT',
    tone                text,
    version_number      int         not null default 1,
    next_action_id      uuid,
    created_at          timestamptz not null default now(),
    copied_at           timestamptz,
    sent_at             timestamptz
);

create index idx_messages_user_id on outreach_messages (user_id);
create index idx_messages_contact on outreach_messages (contact_id);
create index idx_messages_vacancy on outreach_messages (vacancy_id);

create table next_actions (
    id                         uuid        not null default gen_random_uuid() primary key,
    user_id                    uuid        not null references users (id) on delete cascade,
    action_type                text        not null,
    priority                   text        not null default 'P2',
    priority_score             int         not null default 50,
    due_date                   date        not null,
    status                     text        not null default 'PENDING',
    reason                     text        not null,
    company_id                 uuid        references companies (id) on delete set null,
    vacancy_id                 uuid        references vacancies (id) on delete set null,
    application_id             uuid        references applications (id) on delete set null,
    contact_id                 uuid        references contacts (id) on delete set null,
    message_id                 uuid        references outreach_messages (id) on delete set null,
    saved_search_id            uuid        references saved_searches (id) on delete set null,
    generated_message_required boolean     not null default false,
    recommended_message_type   text,
    snoozed_until              date,
    created_at                 timestamptz not null default now(),
    completed_at               timestamptz
);

create index idx_next_actions_user_id on next_actions (user_id);
create index idx_next_actions_user_status on next_actions (user_id, status);
create index idx_next_actions_user_due on next_actions (user_id, due_date);
create index idx_next_actions_user_priority on next_actions (user_id, priority_score desc);
