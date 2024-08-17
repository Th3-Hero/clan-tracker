package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.jobs.MemberActivityFetchJob;
import com.th3hero.clantracker.app.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulingService {
    private final Scheduler scheduler;
    private final ConfigService configService;

    private static final String MEMBER_ACTIVITY_FETCH_GROUP = "member_activity_fetch_group";

    public void scheduleMemberActivityFetchJob(Long clanId) {
        try {
            createJobIfNone(MemberActivityFetchJob.JOB_KEY, MemberActivityFetchJob.class, "Fetches member activity for a clan");

            int interval = configService.getConfigJpa().getMemberActivityUpdateInterval();

            TriggerKey key = TriggerKey.triggerKey(clanId.toString(), MEMBER_ACTIVITY_FETCH_GROUP);
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(key)
                .forJob(MemberActivityFetchJob.JOB_KEY)
                .usingJobData(MemberActivityFetchJob.CLAN_ID, clanId)
                .startAt(DateUtils.toDate(nextExecutionTime(interval)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInHours(interval)
                    .withMisfireHandlingInstructionIgnoreMisfires()
                    .repeatForever())
                .build();

            scheduler.scheduleJob(trigger);
            log.info("Scheduled new trigger for member activity fetch of clan: {}", clanId);
        } catch (SchedulerException e) {
            log.error("Failed to schedule new job/trigger for member activity fetch job", e);
            throw new SchedulingException("Failed to schedule new job/trigger for member activity fetch job", e);
        }
    }

    public void changeMemberActivityFetchJobInterval(Long clanId) {
        try {
            Trigger olderTrigger = scheduler.getTrigger(TriggerKey.triggerKey(clanId.toString(), MEMBER_ACTIVITY_FETCH_GROUP));

            Integer interval = configService.getConfigJpa().getMemberActivityUpdateInterval();

            Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(olderTrigger.getKey())
                .forJob(MemberActivityFetchJob.JOB_KEY)
                .usingJobData(MemberActivityFetchJob.CLAN_ID, clanId)
                .startAt(DateUtils.toDate(nextExecutionTime(interval)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInHours(interval)
                    .withMisfireHandlingInstructionIgnoreMisfires()
                    .repeatForever())
                .build();

            scheduler.rescheduleJob(olderTrigger.getKey(), newTrigger);
            log.info("Changed interval for member activity fetch of clan: {}", clanId);
        } catch (SchedulerException e) {
            log.error("Failed to change interval for member activity fetch job", e);
            throw new SchedulingException("Failed to change interval for member activity fetch job", e);
        }
    }

    public void removeMemberActivityFetchJob(Long clanId) {
        try {
            scheduler.unscheduleJob(TriggerKey.triggerKey(clanId.toString(), MEMBER_ACTIVITY_FETCH_GROUP));
            log.info("Removed trigger for member activity fetch of clan: {}", clanId);
        } catch (SchedulerException e) {
            log.error("Failed to remove job/trigger for member activity fetch job", e);
            throw new SchedulingException("Failed to remove job/trigger for member activity fetch job", e);
        }
    }

    /**
     * Creates a quartz job if one does not already exist.
     *
     * @param jobKey The key of the job
     * @param jobClass The class of the job
     * @param description The description of the job
     * @throws SchedulerException If the job cannot be created
     */
    private <T extends Job> void createJobIfNone(JobKey jobKey, Class<T> jobClass, String description) throws SchedulerException {
        if (!scheduler.checkExists(jobKey)) {
            JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .storeDurably()
                .withDescription(description)
                .build();
            scheduler.addJob(job, true);
            log.info("Added job: {}", jobKey.getName());
        }
    }

    private LocalDateTime nextExecutionTime(int interval) {
        var date = LocalDate.now().atStartOfDay();
        while (date.isBefore(LocalDateTime.now())) {
            date = date.plusHours(interval);
        }
        return date;
    }

}
