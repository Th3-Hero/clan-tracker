-- Create the new tables
alter table member
    rename to member_old;

create table player (
    id bigint primary key,
    name text not null
);

create table member (
    player_id bigint not null,
    clan_id bigint not null,
    rank text not null,
    joined_clan timestamp not null,
    last_updated timestamp not null,
    foreign key (clan_id)
        references clan (id),
    foreign key (player_id)
        references player (id),
    primary key (player_id, clan_id)
);
create index member_player_id_idx on member (player_id);
create index member_clan_id_idx on member (clan_id);

create table player_snapshot (
    player_id bigint not null,
    fetched_at timestamp not null,
    name text not null,
    clan_id bigint not null,
    rank text not null,
    joined_at timestamp not null,
    foreign key (player_id)
        references player (id),
    primary key (player_id, fetched_at)
);
create index player_snapshot_player_id_idx on player_snapshot (player_id);

create table player_activity (
    player_id bigint not null,
    fetched_at timestamp not null,
    last_battle timestamp not null,
    total_random_battles bigint not null check ( total_random_battles >= 0 ),
    total_skirmish_battles bigint not null check ( total_skirmish_battles >= 0 ),
    total_advances_battles bigint not null check ( total_advances_battles >= 0 ),
    total_clan_war_battles bigint not null check ( total_clan_war_battles >= 0 ),
    foreign key (player_id)
        references player (id),
    primary key (player_id, fetched_at)
);
create index player_activity_player_id_idx on player_activity (player_id);

alter table clan_members
    rename clan_jpa_id to clan_id;
alter table clan_members
    rename members_id to member_id;

-- -- Move over the data
insert into player (id, name)
    select id, name
        from member_old;
insert into player (id, name)
    select distinct on (member_id) member_id, name
        from member_activity
    on conflict (id) do nothing;

insert into player_snapshot (
    player_id,
    fetched_at,
    clan_id,
    name,
    rank,
    joined_at)
    select member_id,
           updated_at,
           clan_id,
           name,
           rank,
           joined_clan
        from member_activity;

insert into player_activity (
    player_id,
    fetched_at,
    last_battle,
    total_random_battles,
    total_skirmish_battles,
    total_advances_battles,
    total_clan_war_battles)
    select member_id,
           updated_at,
           last_battle,
           total_random_battles,
           total_skirmish_battles,
           total_advances_battles,
           total_clan_war_battles
        from member_activity;

insert into member (
    player_id,
    clan_id,
    rank,
    joined_clan,
    last_updated)
    select id,
           clan_jpa_id,
           rank,
           joined_clan,
           last_updated
        from member_old;

-- Cleanup old table data
-- member activity was replaced by player activity and player snapshot
-- member table was replaced by player table and updated to use composite key
drop table member_old cascade;
drop table member_activity;
drop table clan_members;