package com.th3hero.clantracker.app.jobs;

import com.th3hero.clantracker.app.services.ClanTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

@Slf4j
@RequiredArgsConstructor
public class MemberActivityFetchJob implements Job {
    public static final JobKey JOB_KEY = JobKey.jobKey("member_activity_fetch");
    public static final String CLAN_ID = "clan_id";

    private final ClanTrackerService clanTrackerService;

    @Override
    public void execute(JobExecutionContext executionContext) {
        Long clanId = executionContext.getTrigger().getJobDataMap().getLong(CLAN_ID);
        clanTrackerService.fetchClanMembers(clanId);
    }
}
