package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.MemberActivity;
import com.th3hero.clantracker.api.ui.PlayerInfo;
import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.TestEntities;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityRepository;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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
    private PlayerRepository playerRepository;
    @Mock
    private PlayerActivityRepository playerActivityRepository;
    @Mock
    private PlayerSnapshotRepository playerSnapshotRepository;

    @InjectMocks
    private DataRetrievalService dataRetrievalService;

    @SuppressWarnings("unchecked")
    @Test
    void getClanActivityData() {
        final var config = TestEntities.configJpa();
        final var startDate = LocalDate.now();
        final var endDate = LocalDate.now().plusWeeks(1);
        final var clan = TestEntities.clanJpa(1);
        final var memberOne = TestEntities.memberJpa(1, clan);
        final var memberTwo = TestEntities.memberJpa(2, clan);
        final var memberThree = TestEntities.memberJpa(3, clan);
        final var memberActivityListOne = createPlayerActivityList(memberOne);
        final var memberActivityListTwo = createPlayerActivityList(memberTwo);
        final var memberActivityListThree = createPlayerActivityList(memberThree);

        when(configService.getConfigJpa())
            .thenReturn(config);
        when(clanRepository.findById(memberOne.getClanJpa().getId()))
            .thenReturn(Optional.of(memberOne.getClanJpa()));
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(
                memberActivityListOne,
                memberActivityListTwo,
                memberActivityListThree
            );

        final var result = dataRetrievalService.getClanActivityData(memberOne.getClanJpa().getId(), startDate, endDate);

        assertThat(result.clan().id()).isEqualTo(clan.getId());
        assertThat(result.clan().tag()).isEqualTo(clan.getTag());
        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);

        assertThat(result.memberActivity())
            .satisfiesExactly(
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberOne.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberTwo.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberThree.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                }
            );
    }

    @Test
    void getClanActivityData_missingClan() {
        Long clanId = 1234L;
        final var config = TestEntities.configJpa();
        final var startDate = LocalDate.now();
        final var endDate = LocalDate.now().plusDays(1);

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
        final var memberActivityListOne = createPlayerActivityList(memberOne);
        final var memberActivityListTwo = createPlayerActivityList(memberTwo);
        final var memberActivityListThree = createPlayerActivityList(memberThree);

        when(configService.getConfigJpa())
            .thenReturn(config);
        when(clanRepository.findById(memberOne.getClanJpa().getId()))
            .thenReturn(Optional.of(memberOne.getClanJpa()));
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(memberActivityListOne, memberActivityListTwo, memberActivityListThree);

        final var result = dataRetrievalService.getClanActivityData(memberOne.getClanJpa().getId(), null, null);

        assertThat(result.clan().id()).isEqualTo(clan.getId());
        assertThat(result.clan().tag()).isEqualTo(clan.getTag());
        assertThat(result.startDate()).isEqualTo(result.endDate().minusDays(config.getDefaultActivitySummaryDateRange()));

        assertThat(result.memberActivity())
            .satisfiesExactly(
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberOne.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberTwo.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                },
                memberActivity -> {
                    assertThat(memberActivity.memberId()).isEqualTo(memberThree.getPlayerJpa().getId());
                    assertPlayerActivity(memberActivity);
                }
            );

    }

    @Test
    void getClanActivityData_startDateAfterEndDate() {
        Long clanId = 1234L;
        final var config = TestEntities.configJpa();
        final var clan = TestEntities.clanJpa(1);
        final var startDate = LocalDate.now();
        final var endDate = LocalDate.now().minusDays(1);

        when(configService.getConfigJpa()).
            thenReturn(config);
        when(clanRepository.findById(clanId)).
            thenReturn(Optional.of(clan));

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> dataRetrievalService.getClanActivityData(clanId, startDate, endDate));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPlayerActivity_validId() {
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusMonths(1);
        var player = TestEntities.playerJpa(1);
        var playerActivityList = createPlayerActivityList(TestEntities.memberJpa(1, TestEntities.clanJpa(1)));

        when(playerRepository.findById(player.getId()))
            .thenReturn(Optional.of(player));
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(playerActivityList);

        var result = dataRetrievalService.getPlayerActivity(player.getId().toString(), startDate, endDate);

        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.playerInfo()).hasSize(1);
        assertThat(result.playerInfo().getFirst().id()).isEqualTo(player.getId());
        assertPlayerActivity(result.playerInfo().getFirst());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPlayerActivity_validName() {
        var idOrName = "bob123";
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusMonths(1);
        var player = TestEntities.playerJpa(1);
        var playerActivityList = createPlayerActivityList(TestEntities.memberJpa(1, TestEntities.clanJpa(1)));
        var playerSnapshotList = createPlayerSnapshotList(player);

        when(playerSnapshotRepository.findByNameContaining(idOrName))
            .thenReturn(playerSnapshotList);
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(playerActivityList);

        var result = dataRetrievalService.getPlayerActivity(idOrName, startDate, endDate);

        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.playerInfo()).hasSize(1);
        assertThat(result.playerInfo().getFirst().id()).isEqualTo(player.getId());
        assertPlayerActivity(result.playerInfo().getFirst());
    }

    @Test
    void getPlayerActivity_startDateAfterEndDate() {
        var idOrName = "test";
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().minusDays(1);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> dataRetrievalService.getPlayerActivity(idOrName, startDate, endDate));
    }

    @Test
    void getPlayerActivity_noPlayerWithNameFound() {
        var idOrName = "unknownPlayer";
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusMonths(1);

        when(playerSnapshotRepository.findByNameContaining(idOrName))
            .thenReturn(List.of());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> dataRetrievalService.getPlayerActivity(idOrName, startDate, endDate))
            .withMessageContaining("No player found with the id or name: %s".formatted(idOrName));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPlayerActivity_multipleMatchingPlayersFound() {
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusMonths(1);
        var playerNameSearch = "Test";
        var playerOne = TestEntities.playerJpa(1);
        var playerTwo = TestEntities.playerJpa(2);
        var playerActivityListOne = createPlayerActivityList(TestEntities.memberJpa(1, TestEntities.clanJpa(1)));
        var playerActivityListTwo = createPlayerActivityList(TestEntities.memberJpa(2, TestEntities.clanJpa(1)));
        var playerSnapshotList = new ArrayList<PlayerSnapshotJpa>();
        playerSnapshotList.addAll(createPlayerSnapshotList(playerOne));
        playerSnapshotList.addAll(createPlayerSnapshotList(playerTwo));

        when(playerSnapshotRepository.findByNameContaining(playerNameSearch))
            .thenReturn(playerSnapshotList);
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(playerActivityListOne, playerActivityListTwo);

        var result = dataRetrievalService.getPlayerActivity(playerNameSearch, startDate, endDate);

        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.playerInfo()).hasSize(2);
        assertThat(result.playerInfo()).extracting(PlayerInfo::id)
            .containsExactlyInAnyOrder(playerOne.getId(), playerTwo.getId());
        assertThat(result.playerInfo()).extracting(PlayerInfo::name)
            .containsExactlyInAnyOrder(playerOne.getName(), playerTwo.getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPlayerActivity_noActivityWithinPeriod() {
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusMonths(1);
        var player = TestEntities.playerJpa(1);
        var playerId = player.getId().toString();

        when(playerRepository.findById(player.getId()))
            .thenReturn(Optional.of(player));
        when(playerActivityRepository.findBy(any(Specification.class), any()))
            .thenReturn(List.of());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> dataRetrievalService.getPlayerActivity(playerId, startDate, endDate))
            .withMessageContaining("No player activity found within the specified time period");
    }

    private List<PlayerActivityJpa> createPlayerActivityList(MemberJpa member) {
        List<PlayerActivityJpa> activityJpas = new ArrayList<>();
        activityJpas.add(
            PlayerActivityJpa.create(
                member.getPlayerJpa(),
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now(),
                1L + member.getPlayerJpa().getId(),
                1L + member.getPlayerJpa().getId(),
                1L + member.getPlayerJpa().getId(),
                1L + member.getPlayerJpa().getId(),
                LocalDate.now()
            )
        );
        activityJpas.add(
            PlayerActivityJpa.create(
                member.getPlayerJpa(),
                LocalDateTime.now().minusWeeks(1),
                LocalDateTime.now(),
                5L + member.getPlayerJpa().getId(),
                10L + member.getPlayerJpa().getId(),
                15L + member.getPlayerJpa().getId(),
                20L + member.getPlayerJpa().getId(),
                LocalDate.now().minusDays(4)
            )
        );
        activityJpas.add(
            PlayerActivityJpa.create(
                member.getPlayerJpa(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                10L + member.getPlayerJpa().getId(),
                20L + member.getPlayerJpa().getId(),
                30L + member.getPlayerJpa().getId(),
                40L + member.getPlayerJpa().getId(),
                LocalDate.now()
            )
        );
        return activityJpas;
    }

    private List<PlayerSnapshotJpa> createPlayerSnapshotList(PlayerJpa playerJpa) {
        List<PlayerSnapshotJpa> playerSnapshotJpas = new ArrayList<>();
        playerSnapshotJpas.add(
            PlayerSnapshotJpa.create(
                playerJpa,
                LocalDateTime.now().minusMonths(1),
                TestEntities.clanJpa(1),
                "Bob",
                Rank.COMBAT_OFFICER,
                LocalDateTime.now().minusMonths(2),
                LocalDate.now().minusMonths(1)
            )
        );
        playerSnapshotJpas.add(
            PlayerSnapshotJpa.create(
                playerJpa,
                LocalDateTime.now().minusWeeks(1),
                TestEntities.clanJpa(1),
                "Bob1345",
                Rank.PERSONNEL_OFFICER,
                LocalDateTime.now().minusMonths(2),
                LocalDate.now()
            )
        );
        playerSnapshotJpas.add(
            PlayerSnapshotJpa.create(
                playerJpa,
                LocalDateTime.now(),
                TestEntities.clanJpa(1),
                "Bob123",
                Rank.PRIVATE,
                LocalDateTime.now().minusMonths(2),
                LocalDate.now()
            )
        );
        return playerSnapshotJpas;
    }

    private void assertPlayerActivity(MemberActivity memberActivity) {
        assertThat(memberActivity.randomsDiff()).isEqualTo(9);
        assertThat(memberActivity.skirmishDiff()).isEqualTo(19);
        assertThat(memberActivity.advancesDiff()).isEqualTo(29);
        assertThat(memberActivity.clanWarDiff()).isEqualTo(39);
    }

    private void assertPlayerActivity(PlayerInfo playerInfo) {
        assertThat(playerInfo.randomsDiff()).isEqualTo(9);
        assertThat(playerInfo.skirmishDiff()).isEqualTo(19);
        assertThat(playerInfo.advancesDiff()).isEqualTo(29);
        assertThat(playerInfo.clanWarDiff()).isEqualTo(39);
    }
}
