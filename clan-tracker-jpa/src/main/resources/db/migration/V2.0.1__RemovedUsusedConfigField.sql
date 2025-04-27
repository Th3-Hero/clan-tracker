
-- Remove unused config field performance_threshold_bad
alter table config
    drop column performance_threshold_bad;