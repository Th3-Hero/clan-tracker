create sequence config_id_seq start with 1 increment by 1;
create table config (
    id bigint primary key,
    member_activity_update_interval integer not null,
    performance_threshold_bad integer not null,
    performance_threshold_poor integer not null,
    performance_threshold_good integer not null,
    default_activity_summary_date_range integer not null
);

create table clan (
    id bigint primary key,
    tag text not null
);

create table member (
    id bigint primary key,
    name text not null,
    clan_jpa_id bigint,
    rank text not null,
    joined_clan timestamp not null,
    last_updated timestamp not null,
    foreign key (clan_jpa_id)
        references clan (id)
);

create table member_activity (
    member_id bigint not null,
    updated_at timestamp not null,
    name text not null,
    rank text not null,
    clan_id bigint not null,
    joined_clan timestamp not null,
    last_battle timestamp not null,
    total_random_battles bigint not null,
    total_skirmish_battles bigint not null,
    total_advances_battles bigint not null,
    total_clan_war_battles bigint not null,
    primary key (member_id, updated_at)
);

create table clan_members (
    clan_jpa_id bigint not null,
    members_id bigint not null,
    primary key (clan_jpa_id, members_id),
    foreign key (clan_jpa_id)
        references clan (id),
    foreign key (members_id)
        references member (id)
);



----------------------------------------
------------ Quartz tables -------------
----------------------------------------
create table qrtz_job_details (
    sched_name varchar(120) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250) null,
    job_class_name varchar(250) not null,
    is_durable boolean not null,
    is_nonconcurrent boolean not null,
    is_update_data boolean not null,
    requests_recovery boolean not null,
    job_data bytea null,
    primary key (sched_name, job_name, job_group)
);

create table qrtz_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250) null,
    next_fire_time bigint null,
    prev_fire_time bigint null,
    priority integer null,
    trigger_state varchar(16) not null,
    trigger_type varchar(8) not null,
    start_time bigint not null,
    end_time bigint null,
    calendar_name varchar(200) null,
    misfire_instr smallint null,
    job_data bytea null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group)
        references qrtz_job_details (sched_name, job_name, job_group)
);

create table qrtz_simple_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    repeat_count bigint not null,
    repeat_interval bigint not null,
    times_triggered bigint not null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
);

create table qrtz_cron_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    cron_expression varchar(120) not null,
    time_zone_id varchar(80),
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
);

create table qrtz_simprop_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1 varchar(512) null,
    str_prop_2 varchar(512) null,
    str_prop_3 varchar(512) null,
    int_prop_1 int null,
    int_prop_2 int null,
    long_prop_1 bigint null,
    long_prop_2 bigint null,
    dec_prop_1 numeric(13, 4) null,
    dec_prop_2 numeric(13, 4) null,
    bool_prop_1 boolean null,
    bool_prop_2 boolean null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
);

create table qrtz_blob_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    blob_data bytea null,
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group)
        references qrtz_triggers (sched_name, trigger_name, trigger_group)
);

create table qrtz_calendars (
    sched_name varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar bytea not null,
    primary key (sched_name, calendar_name)
);

create table qrtz_paused_trigger_grps (
    sched_name varchar(120) not null,
    trigger_group varchar(200) not null,
    primary key (sched_name, trigger_group)
);

create table qrtz_fired_triggers (
    sched_name varchar(120) not null,
    entry_id varchar(95) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    instance_name varchar(200) not null,
    fired_time bigint not null,
    sched_time bigint not null,
    priority integer not null,
    state varchar(16) not null,
    job_name varchar(200) null,
    job_group varchar(200) null,
    is_nonconcurrent boolean null,
    requests_recovery boolean null,
    primary key (sched_name, entry_id)
);

create table qrtz_scheduler_state (
    sched_name varchar(120) not null,
    instance_name varchar(200) not null,
    last_checkin_time bigint not null,
    checkin_interval bigint not null,
    primary key (sched_name, instance_name)
);

create table qrtz_locks (
    sched_name varchar(120) not null,
    lock_name varchar(40) not null,
    primary key (sched_name, lock_name)
);

create index idx_qrtz_j_req_recovery on qrtz_job_details (sched_name, requests_recovery);
create index idx_qrtz_j_grp on qrtz_job_details (sched_name, job_group);

create index idx_qrtz_t_j on qrtz_triggers (sched_name, job_name, job_group);
create index idx_qrtz_t_jg on qrtz_triggers (sched_name, job_group);
create index idx_qrtz_t_c on qrtz_triggers (sched_name, calendar_name);
create index idx_qrtz_t_g on qrtz_triggers (sched_name, trigger_group);
create index idx_qrtz_t_state on qrtz_triggers (sched_name, trigger_state);
create index idx_qrtz_t_n_state on qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
create index idx_qrtz_t_n_g_state on qrtz_triggers (sched_name, trigger_group, trigger_state);
create index idx_qrtz_t_next_fire_time on qrtz_triggers (sched_name, next_fire_time);
create index idx_qrtz_t_nft_st on qrtz_triggers (sched_name, trigger_state, next_fire_time);
create index idx_qrtz_t_nft_misfire on qrtz_triggers (sched_name, misfire_instr, next_fire_time);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group,
                                                             trigger_state);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers (sched_name, instance_name);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers (sched_name, job_name, job_group);
create index idx_qrtz_ft_jg on qrtz_fired_triggers (sched_name, job_group);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
create index idx_qrtz_ft_tg on qrtz_fired_triggers (sched_name, trigger_group);