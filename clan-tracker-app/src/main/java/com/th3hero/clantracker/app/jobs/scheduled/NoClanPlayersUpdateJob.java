package com.th3hero.clantracker.app.jobs.scheduled;

import com.th3hero.clantracker.app.services.ApiService;
import com.th3hero.clantracker.app.wargaming.PlayerInfo;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoClanPlayersUpdateJob {
    private final ApiService apiService;
    private final PlayerRepository playerRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * *")
    public void updateNoClanPlayers() {
        List<PlayerJpa> clanlessPlayers = playerRepository.findPlayersWithoutClan();
        if (clanlessPlayers.isEmpty()) {
            log.debug("No players without a clan");
            return;
        }

        log.debug("{} players without a clan", clanlessPlayers.size());
        List<Long> playerIds = clanlessPlayers.stream()
            .map(PlayerJpa::getId)
            .toList();
        Map<Long, PlayerInfo.Player> updatedPlayers = apiService.playerInfo(playerIds);

        List<PlayerJpa> updatedPlayersList = new ArrayList<>();
        for (PlayerJpa player : clanlessPlayers) {
            if (!updatedPlayers.get(player.getId()).name().equals(player.getName())) {
                player.setName(updatedPlayers.get(player.getId()).name());
                updatedPlayersList.add(player);
            }
        }
        log.debug("Updating names for {} players", updatedPlayersList.size());
        playerRepository.saveAll(updatedPlayersList);
    }
}
