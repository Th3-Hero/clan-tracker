create sequence config_id_seq start with 1 increment by 1;
create table config (
    id bigint primary key,
    performance_threshold_bad integer not null,
    performance_threshold_poor integer not null,
    performance_threshold_good integer not null,
    default_activity_summary_date_range integer not null,
    default_probation_period integer not null
);

create table config_ranks_allowed_to_edit (
    config_id bigint not null,
    rank text not null,
    foreign key (config_id)
        references config (id)
);

create table clan (
    id bigint primary key,
    name text not null
);

create table player (
    id bigint primary key,
    name text not null,
    clan_id bigint,
    rank text not null,
    joined_clan timestamp not null,
    last_updated timestamp not null,
    foreign key (clan_id)
        references clan (id)
);

create table player_activity (
    player_id bigint not null,
    fetch_time timestamp not null,
    last_battle timestamp not null,
    total_random_battles bigint not null,
    total_skirmish_battles bigint not null,
    total_advances_battles bigint not null,
    total_clan_war_battles bigint not null,
    primary key (player_id, fetch_time),
    foreign key (player_id)
        references player (id)
);

create table clan_members (
    clan_id bigint not null,
    player_id bigint not null,
    primary key (clan_id, player_id),
    foreign key (clan_id)
        references clan (id),
    foreign key (player_id)
        references player (id)
);