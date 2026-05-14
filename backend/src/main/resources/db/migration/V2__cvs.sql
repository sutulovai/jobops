create table cvs (
    id                uuid        not null default gen_random_uuid() primary key,
    user_id           uuid        not null references users (id) on delete cascade,
    label             text        not null,
    version           int         not null default 1,
    is_default        boolean     not null default false,
    original_filename text        not null,
    storage_path      text        not null,
    extracted_text    text,
    mime_type         text        not null default 'application/pdf',
    file_size_bytes   bigint,
    created_at        timestamptz not null default now(),
    updated_at        timestamptz not null default now()
);

create index idx_cvs_user_id on cvs (user_id);

-- Only one default CV per user
create unique index idx_cvs_user_default on cvs (user_id) where is_default = true;
