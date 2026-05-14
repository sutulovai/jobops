alter table weekly_reviews
    add column if not exists applications_target   int          not null default 0,
    add column if not exists next_week_targets      text,
    add column if not exists ai_recommendations     text,
    add column if not exists week_end_date          date;

-- back-fill week_end_date for any existing rows
update weekly_reviews set week_end_date = week_start_date + interval '6 days' where week_end_date is null;
