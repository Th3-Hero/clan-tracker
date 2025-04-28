
-- Add new columns to player_activity and player_snapshot tables
alter table player_activity
    add column effective_date date;

alter table player_snapshot
    add column effective_date date;


-- Move over the data
update player_activity
    set
        effective_date = (fetched_at - interval '3 hours')::date;

update player_snapshot
    set
        effective_date = (fetched_at - interval '3 hours')::date;

-- Add not null constraints
alter table player_activity
    alter column effective_date
        set not null;

alter table player_snapshot
    alter column effective_date
        set not null;

-- Add indexes
create index player_activity_effective_date_idx on player_activity (effective_date);
create index player_snapshot_effective_date_idx on player_snapshot (effective_date);