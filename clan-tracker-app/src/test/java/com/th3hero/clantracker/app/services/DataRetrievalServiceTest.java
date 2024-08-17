package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.TestEntities;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.jpa.entities.MemberActivityJpa;
import com.th3hero.clantracker.jpa.entities.MemberJpa;
import com.th3hero.clantracker.jpa.repositories.ClanRepository;
import com.th3hero.clantracker.jpa.repositories.MemberActivityRepository;
import com.th3hero.clantracker.api.ui.MemberActivity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataRetrievalServiceTest {
    @Mock
    private ConfigService configService;
    @Mock
    private ClanRepository clanRepository;
    @Mock
    private MemberActivityRepository memberActivityRepository;

    @InjectMocks
    private DataRetrievalService dataRetrievalService;

    @SuppressWarnings("unchecked")
    @Test
    void getClanActivityData() {
        final var config = TestEntities.configJpa();
        final var startDate = LocalDateTime.now();
        final var endDate = LocalDateTime.now().plusWeeks(1);
        final var clan = TestEntities.clanJpa(1);
        final var memberOne = TestEntities.memberJpa(1, clan);
        final var memberTwo = TestEntities.memberJpa(2, clan);
        final var memberThree = TestEntities.memberJpa(3, clan);
        final var memberActivityListOne = createMemberActivityList(memberOne);
        final var memberActivityListTwo = createMemberActivityList(memberTwo);
        final var memberActivityListThree = createMemberActivityList(memberThree);

        when(configService.getConfigJpa())
            .thenReturn(config);
        when(clanRepository.findById(memberOne.getClanJpa().getId()))
            .thenReturn(Optional.of(memberOne.getClanJpa()));
        when(memberActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(memberActivityListOne, memberActivityListTwo, memberActivityListThree);

        final var result = dataRetrievalService.getClanActivityData(memberOne.getClanJpa().getId(), startDate, endDate);

        assertThat(result.clan().id()).isEqualTo(clan.getId());
        assertThat(result.clan().tag()).isEqualTo(clan.getTag());
        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.defaultPerformanceThresholdBad()).isEqualTo(config.getPerformanceThresholdBad());
        assertThat(result.defaultPerformanceThresholdPoor()).isEqualTo(config.getPerformanceThresholdPoor());
        assertThat(result.defaultPerformanceThresholdGood()).isEqualTo(config.getPerformanceThresholdGood());

        assertThat(result.memberActivity())
            .satisfiesExactly(
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberOne.getId());
                    assertMemberActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberTwo.getId());
                    assertMemberActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberThree.getId());
                    assertMemberActivity(memberActivity);
                }
            );
    }

    @Test
    void getClanActivityData_missingClan() {
        Long clanId = 1234L;
        final var config = TestEntities.configJpa();
        final var startDate = LocalDateTime.now();
        final var endDate = LocalDateTime.now().plusDays(1);

        when(configService.getConfigJpa())
            .thenReturn(config);
        when(clanRepository.findById(clanId))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(ClanNotFoundException.class)
            .isThrownBy(() -> dataRetrievalService.getClanActivityData(clanId, startDate, endDate));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getClanActivityData_noSpecifiedDateRange() {
        final var config = TestEntities.configJpa();
        final var clan = TestEntities.clanJpa(1);
        final var memberOne = TestEntities.memberJpa(1, clan);
        final var memberTwo = TestEntities.memberJpa(2, clan);
        final var memberThree = TestEntities.memberJpa(3, clan);
        final var memberActivityListOne = createMemberActivityList(memberOne);
        final var memberActivityListTwo = createMemberActivityList(memberTwo);
        final var memberActivityListThree = createMemberActivityList(memberThree);

        when(configService.getConfigJpa())
            .thenReturn(config);
        when(clanRepository.findById(memberOne.getClanJpa().getId()))
            .thenReturn(Optional.of(memberOne.getClanJpa()));
        when(memberActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(memberActivityListOne, memberActivityListTwo, memberActivityListThree);

        final var result = dataRetrievalService.getClanActivityData(memberOne.getClanJpa().getId(), null, null);

        assertThat(result.clan().id()).isEqualTo(clan.getId());
        assertThat(result.clan().tag()).isEqualTo(clan.getTag());
        assertThat(result.startDate()).isEqualTo(result.endDate().minusDays(config.getDefaultActivitySummaryDateRange()));
        assertThat(result.defaultPerformanceThresholdBad()).isEqualTo(config.getPerformanceThresholdBad());
        assertThat(result.defaultPerformanceThresholdPoor()).isEqualTo(config.getPerformanceThresholdPoor());
        assertThat(result.defaultPerformanceThresholdGood()).isEqualTo(config.getPerformanceThresholdGood());

        assertThat(result.memberActivity())
            .satisfiesExactly(
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberOne.getId());
                    assertMemberActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberTwo.getId());
                    assertMemberActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberThree.getId());
                    assertMemberActivity(memberActivity);
                }
            );

    }

    @Test
    void getClanActivityData_startDateAfterEndDate() {
        Long clanId = 1234L;
        final var config = TestEntities.configJpa();
        final var clan = TestEntities.clanJpa(1);
        final var startDate = LocalDateTime.now();
        final var endDate = LocalDateTime.now().minusDays(1);

        when(configService.getConfigJpa()).
            thenReturn(config);
        when(clanRepository.findById(clanId)).
            thenReturn(Optional.of(clan));

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> dataRetrievalService.getClanActivityData(clanId, startDate, endDate));
    }

    private List<MemberActivityJpa> createMemberActivityList(MemberJpa member) {
        List<MemberActivityJpa> activityJpas = new ArrayList<>();
        activityJpas.add(
            MemberActivityJpa.create(
                member.getId(),
                LocalDateTime.now().minusMonths(1),
                member.getName(),
                member.getRank(),
                member.getClanJpa().getId(),
                member.getJoinedClan(),
                LocalDateTime.now(),
                1L + member.getId(),
                1L + member.getId(),
                1L + member.getId(),
                1L + member.getId()
            )
        );
        activityJpas.add(
            MemberActivityJpa.create(
                member.getId(),
                LocalDateTime.now().minusWeeks(1),
                member.getName(),
                member.getRank(),
                member.getClanJpa().getId(),
                member.getJoinedClan(),
                LocalDateTime.now(),
                5L + member.getId(),
                10L + member.getId(),
                15L + member.getId(),
                20L + member.getId()
            )
        );
        activityJpas.add(
            MemberActivityJpa.create(
                member.getId(),
                LocalDateTime.now(),
                member.getName(),
                member.getRank(),
                member.getClanJpa().getId(),
                member.getJoinedClan(),
                LocalDateTime.now(),
                10L + member.getId(),
                20L + member.getId(),
                30L + member.getId(),
                40L + member.getId()
            )
        );
        return activityJpas;
    }

    private void assertMemberActivity(MemberActivity memberActivity) {
        assertThat(memberActivity.randomsDiff()).isEqualTo(9);
        assertThat(memberActivity.skirmishDiff()).isEqualTo(19);
        assertThat(memberActivity.advancesDiff()).isEqualTo(29);
        assertThat(memberActivity.clanWarDiff()).isEqualTo(39);
    }
}
