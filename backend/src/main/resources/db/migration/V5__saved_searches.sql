create table saved_searches (
    id                         uuid        not null default gen_random_uuid() primary key,
    user_id                    uuid        not null references users (id) on delete cascade,
    title                      text        not null,
    platform                   text        not null default 'LINKEDIN',
    url                        text,
    query_text                 text,
    boolean_query              text,
    city                       text,
    keywords                   text[]      not null default '{}',
    frequency                  text        not null default 'WEEKLY',
    last_checked_date          date,
    next_check_date            date,
    useful                     boolean     not null default true,
    yield_rating               text        not null default 'UNKNOWN',
    jobs_added_count           int         not null default 0,
    applications_created_count int         not null default 0,
    responses_from_count       int         not null default 0,
    notes                      text,
    active                     boolean     not null default true,
    created_at                 timestamptz not null default now(),
    updated_at                 timestamptz not null default now()
);

create index idx_saved_searches_user on saved_searches (user_id);
create index idx_saved_searches_next_check on saved_searches (user_id, next_check_date);
