package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.TestEntities;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.jpa.repositories.ClanRepository;
import com.th3hero.clantracker.jpa.repositories.MemberActivityRepository;
import com.th3hero.clantracker.jpa.repositories.MemberRepository;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan.BasicPlayer;
import com.th3hero.clantracker.app.wargaming.ClanSearch.BasicClan;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer.Battle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanTrackerServiceTest {
    @Mock
    private ApiService apiService;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private ClanRepository clanRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberActivityRepository memberActivityRepository;

    @InjectMocks
    private ClanTrackerService clanTrackerService;


    @Test
    void addClanForTracking() {
        final var clanTag = "CLAN";
        final var clanId = 1234L;
        final var basicClan = new BasicClan(clanId, clanTag);
        final var enrichedClan = getEnrichedClan(clanId, clanTag);
        final var enrichedPlayers = getEnrichedPlayers();

        when(apiService.clanSearch(clanTag))
            .thenReturn(Optional.of(basicClan));
        when(apiService.clanDetails(clanId))
            .thenReturn(Optional.of(enrichedClan));
        when(apiService.memberDetails(List.of(1L, 2L, 3L)))
            .thenReturn(enrichedPlayers);
        when(clanRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        clanTrackerService.addClanForTracking(clanTag);

        verify(schedulingService).scheduleMemberActivityFetchJob(clanId);
    }

    @Test
    void addClanForTracking_missingClan() {
        final var clanTag = "CLAN";

        when(apiService.clanSearch(clanTag))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(ClanNotFoundException.class)
            .isThrownBy(() -> clanTrackerService.addClanForTracking(clanTag));
    }

    @Test
    void removeClanFromTracking() {
        final var clanTag = "CLAN";
        final var clan = TestEntities.clanJpa(1);

        when(clanRepository.findByTag(clanTag))
            .thenReturn(Optional.of(clan));

        clanTrackerService.removeClanFromTracking(clanTag);

        verify(schedulingService).removeMemberActivityFetchJob(clan.getId());
        verify(memberRepository).deleteAll(clan.getMembers());
        verify(clanRepository).delete(clan);
    }

    @Test
    void removeClanFromTracking_missingClan() {
        final var clanTag = "CLAN";

        when(clanRepository.findByTag(clanTag))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(ClanNotFoundException.class)
            .isThrownBy(() -> clanTrackerService.removeClanFromTracking(clanTag));
    }

    @Test
    void fetchClanMembers() {
        final var clanTag = "CLAN";
        final var clanId = 1234L;
        final var enrichedClan = getEnrichedClan(clanId, clanTag);
        final var enrichedPlayers = getEnrichedPlayers();

        when(apiService.clanDetails(clanId))
            .thenReturn(Optional.of(enrichedClan));
        when(apiService.memberDetails(List.of(1L, 2L, 3L)))
            .thenReturn(enrichedPlayers);
        when(clanRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        clanTrackerService.fetchClanMembers(clanId);

        verify(memberRepository).saveAll(any());
        verify(memberActivityRepository).saveAll(any());
        verify(clanRepository, times(2)).save(any());
    }

    @Test
    void fetchClanMembers_failedClanDetails() {
        final var clanId = 1234L;

        when(apiService.clanDetails(clanId))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(ClanNotFoundException.class)
            .isThrownBy(() -> clanTrackerService.fetchClanMembers(clanId));
    }

    @Test
    void fetchClanMembers_failedMemberDetails() {
        final var clanTag = "CLAN";
        final var clanId = 1234L;
        final var enrichedClan = getEnrichedClan(clanId, clanTag);
        final var enrichedPlayers = new ArrayList<>(getEnrichedPlayers());
        enrichedPlayers.removeFirst();

        when(apiService.clanDetails(clanId))
            .thenReturn(Optional.of(enrichedClan));
        when(apiService.memberDetails(List.of(1L, 2L, 3L)))
            .thenReturn(enrichedPlayers);

        assertThatExceptionOfType(InvalidWargamingResponseException.class)
            .isThrownBy(() -> clanTrackerService.fetchClanMembers(clanId));
    }

    private static EnrichedClan getEnrichedClan(long clanId, String clanTag) {
        final var basicPlayers = List.of(
            new BasicPlayer(1L, 1234L, "commander"),
            new BasicPlayer(2L, 4321L, "executive_officer"),
            new BasicPlayer(3L, 54325L, "recruit")
        );
        return new EnrichedClan(clanId, clanTag, basicPlayers);
    }

    private static List<EnrichedPlayer> getEnrichedPlayers() {
        final var stats = Map.of(
            "random", new Battle(56L),
            "stronghold_skirmish", new Battle(78L),
            "stronghold_defense", new Battle(90L),
            "globalmap_absolute", new Battle(12L),
            "globalmap_middle", new Battle(34L),
            "globalmap_champion", new Battle(56L)
        );
        return List.of(
            new EnrichedPlayer(1L, 1234L, 54321L, 56L, "FRED", stats),
            new EnrichedPlayer(2L, 1234L, 54321L, 56L, "BOB", stats),
            new EnrichedPlayer(3L, 1234L, 54321L, 56L, "ALICE", stats)
        );
    }
}