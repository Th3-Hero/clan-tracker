package com.th3hero.clantracker.app.jobs.scheduled;

import com.th3hero.clantracker.app.services.ApiService;
import com.th3hero.clantracker.app.wargaming.PlayerInfo;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoClanPlayersUpdateJobTest {
    @Mock
    private ApiService apiService;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private NoClanPlayersUpdateJob noClanPlayersUpdateJob;

    @SuppressWarnings("unchecked")
    @Test
    void updateNoClanPlayers() {
        List<PlayerJpa> clanlessPlayers = createPlayers(1L, 2L);
        Map<Long, PlayerInfo.Player> updatedPlayers = Map.of(
            1L, new PlayerInfo.Player("newName1", 1L),
            2L, new PlayerInfo.Player("newName2", 2L)
        );

        when(playerRepository.findPlayersWithoutClan())
            .thenReturn(clanlessPlayers);
        when(apiService.playerInfo(List.of(1L, 2L)))
            .thenReturn(updatedPlayers);

        noClanPlayersUpdateJob.updateNoClanPlayers();

        ArgumentCaptor<Iterable<PlayerJpa>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(playerRepository).saveAll(captor.capture());
        List<PlayerJpa> savedPlayers = (List<PlayerJpa>) captor.getValue();
        assertThat(savedPlayers).hasSize(2);
        assertThat(savedPlayers.get(0).getName()).isEqualTo("newName1");
        assertThat(savedPlayers.get(1).getName()).isEqualTo("newName2");
    }

    @Test
    void updateNoClanPlayers_noPlayersWithoutClan() {
        when(playerRepository.findPlayersWithoutClan())
            .thenReturn(List.of());

        noClanPlayersUpdateJob.updateNoClanPlayers();

        verify(playerRepository, never()).saveAll(any());
    }

    private List<PlayerJpa> createPlayers(Long... ids) {
        List<PlayerJpa> players = new ArrayList<>();
        for (Long id : ids) {
            players.add(PlayerJpa.create(id, "oldName" + id));
        }
        return players;
    }

}