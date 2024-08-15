package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.TestEntities;
import com.th3hero.clantracker.app.jobs.MemberActivityFetchJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.springframework.scheduling.SchedulingException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {
    @Mock
    private Scheduler scheduler;
    @Mock
    private ConfigService configService;

    @InjectMocks
    private SchedulingService schedulingService;

    @Test
    void scheduleMemberActivityFetchJob() throws SchedulerException {
        final var clanId = 1L;
        final var config = TestEntities.configJpa();

        when(scheduler.checkExists(any(JobKey.class)))
            .thenReturn(true);
        when(configService.getConfigJpa())
            .thenReturn(config);

        schedulingService.scheduleMemberActivityFetchJob(clanId);

        verify(scheduler).checkExists(MemberActivityFetchJob.JOB_KEY);
        verify(scheduler).scheduleJob(argThat(trigger ->
            trigger.getJobDataMap().get(MemberActivityFetchJob.CLAN_ID).equals(clanId)
        ));
    }

    @Test
    void scheduleMemberActivityFetchJob_missingJob() throws SchedulerException {
        final var clanId = 1L;
        final var config = TestEntities.configJpa();

        when(scheduler.checkExists(any(JobKey.class)))
            .thenReturn(false);
        when(configService.getConfigJpa())
            .thenReturn(config);

        schedulingService.scheduleMemberActivityFetchJob(clanId);

        verify(scheduler).checkExists(MemberActivityFetchJob.JOB_KEY);
        verify(scheduler).addJob(any(JobDetail.class), eq(true));
        verify(scheduler).scheduleJob(argThat(trigger ->
            trigger.getJobDataMap().get(MemberActivityFetchJob.CLAN_ID).equals(clanId)
        ));
    }

    @Test
    void scheduleMemberActivityFetchJob_schedulerFail() throws SchedulerException {
        final var clanId = 1L;
        final var config = TestEntities.configJpa();

        when(scheduler.checkExists(any(JobKey.class)))
            .thenReturn(true);
        when(configService.getConfigJpa())
            .thenReturn(config);
        when(scheduler.scheduleJob(any(Trigger.class)))
            .thenThrow(SchedulerException.class);

        assertThatExceptionOfType(SchedulingException.class)
            .isThrownBy(() -> schedulingService.scheduleMemberActivityFetchJob(clanId));
    }

    @Test
    void changeMemberActivityFetchJobInterval() throws SchedulerException {
        final Long clanId = 1L;
        final var config = TestEntities.configJpa();
        final var triggerKey = TriggerKey.triggerKey(clanId.toString(), "member_activity_fetch_group");
        final var trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .forJob(MemberActivityFetchJob.JOB_KEY)
            .startNow()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(config.getMemberActivityUpdateInterval())
                .withMisfireHandlingInstructionIgnoreMisfires()
                .repeatForever())
            .build();

        when(scheduler.getTrigger(triggerKey))
            .thenReturn(trigger);
        when(configService.getConfigJpa())
            .thenReturn(config);

        schedulingService.changeMemberActivityFetchJobInterval(clanId);

        verify(scheduler).rescheduleJob(eq(triggerKey), any(Trigger.class));
    }

    @Test
    void changeMemberActivityFetchJobInterval_noExistingTrigger() throws SchedulerException {
        final Long clanId = 1L;
        final var triggerKey = TriggerKey.triggerKey(clanId.toString(), "member_activity_fetch_group");


        when(scheduler.getTrigger(triggerKey))
            .thenThrow(SchedulerException.class);

        assertThatExceptionOfType(SchedulingException.class)
            .isThrownBy(() -> schedulingService.changeMemberActivityFetchJobInterval(clanId));
    }

    @Test
    void removeMemberActivityFetchJob() throws SchedulerException {
        final Long clanId = 1L;
        final var triggerKey = TriggerKey.triggerKey(clanId.toString(), "member_activity_fetch_group");

        schedulingService.removeMemberActivityFetchJob(clanId);

        verify(scheduler).unscheduleJob(triggerKey);
    }

    @Test
    void removeMemberActivityFetchJob_noExistingTrigger() throws SchedulerException {
        final Long clanId = 1L;
        final var triggerKey = TriggerKey.triggerKey(clanId.toString(), "member_activity_fetch_group");

        when(scheduler.unscheduleJob(triggerKey))
            .thenThrow(SchedulerException.class);

        assertThatExceptionOfType(SchedulingException.class)
            .isThrownBy(() -> schedulingService.removeMemberActivityFetchJob(clanId));
    }
}
