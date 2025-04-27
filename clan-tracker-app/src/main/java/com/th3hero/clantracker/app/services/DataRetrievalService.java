package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.*;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.app.utils.ApiFactory;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityRepository;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class DataRetrievalService {
    private final ConfigService configService;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final PlayerActivityRepository playerActivityRepository;
    private final PlayerSnapshotRepository playerSnapshotRepository;

    /**
     * Get the default config values for the app. Configurations are dynamic and thus need to be retrieved.
     *
     * @return the default config values.
     */
    public Config getDefaultConfig() {
        final var configJpa = configService.getConfigJpa();
        return new Config(
            configJpa.getDefaultActivitySummaryDateRange(),
            configJpa.getPerformanceThresholdPoor(),
            configJpa.getPerformanceThresholdGood()
        );
    }

    /**
     * Get a list of all tracked clans.
     *
     * @return a list of tracked clans.
     */
    public List<Clan> getClanList() {
        return clanRepository.findAll().stream()
            .map(clanJpa -> new Clan(clanJpa.getId(), clanJpa.getTag()))
            .toList();
    }

    /**
     * Get activity data for a specific clan within a time period. If start date and/or end date period is not specified, activity info for the last x days is returned(x days being specified by config).
     *
     * @param clanId the id of the clan to get activity data for.
     * @param startDate the start date of the activity data.
     * @param endDate the end date of the activity data.
     * @return the activity data for the clan.
     * @Throws ClanNotFoundException if the clan with the specified id is not found.
     * @Throws IllegalArgumentException if the start date is after the end date.
     */
    public ActivityInfo getClanActivityData(@NonNull Long clanId, LocalDate startDate, LocalDate endDate) {
        ConfigJpa configJpa = configService.getConfigJpa();
        ClanJpa clanJpa = clanRepository.findById(clanId)
            .orElseThrow(() -> new ClanNotFoundException("Failed to find clan with id: %s".formatted(clanId)));

        // if either or both dates are null, use the default range
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = LocalDate.now().minusDays(configJpa.getDefaultActivitySummaryDateRange());
        } else if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<MemberActivity> memberActivity = createMemberActivityList(startDate, endDate, clanJpa);

        return new ActivityInfo(
            new Clan(clanJpa.getId(), clanJpa.getTag()),
            startDate,
            endDate,
            memberActivity
        );

    }

    public PlayerSearch getPlayerActivity(@NotNull String idOrName, @NonNull LocalDate startDate, @NonNull LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<PlayerJpa> players = new ArrayList<>();
        if (idOrName.matches("\\d+")) {
            Long playerId = Long.parseLong(idOrName);
            playerRepository.findById(playerId)
                .ifPresent(players::add);
        } else {
            players = searchPlayer(idOrName);
        }

        if (players.isEmpty()) {
            throw new EntityNotFoundException("No player found with the id or name: %s".formatted(idOrName));
        }

        List<PlayerInfo> playerInfos = new ArrayList<>();

        for (PlayerJpa player : players) {
            List<PlayerActivityJpa> playerActivityJpas = findPlayerActivityJpas(player.getId(), startDate, endDate);
            if (!playerActivityJpas.isEmpty()) {
                playerInfos.add(ApiFactory.createPlayerInfo(player.getId(), playerActivityJpas));
            }
        }

        if (playerInfos.isEmpty()) {
            throw new EntityNotFoundException("No player activity found within the specified time period");
        }

        return new PlayerSearch(startDate, endDate, playerInfos);
    }

    private List<MemberActivity> createMemberActivityList(LocalDate startDate, LocalDate endDate, ClanJpa clanJpa) {
        List<MemberActivity> memberActivity = new ArrayList<>();
        for (MemberJpa member : clanJpa.getMembers()) {
            // get all the member activity data for the player within the specified time period
            List<PlayerActivityJpa> memberActivityJpas = findPlayerActivityJpas(member.getPlayerJpa().getId(), startDate, endDate);

            memberActivity.add(ApiFactory.createMemberActivity(member, memberActivityJpas));
        }
        return memberActivity;
    }

    private List<PlayerJpa> searchPlayer(String name) {
        List<PlayerSnapshotJpa> players = playerSnapshotRepository.findByNameContaining(name);
        return players.stream()
            .map(PlayerSnapshotJpa::getPlayerJpa).distinct().toList();
    }

    private List<PlayerActivityJpa> findPlayerActivityJpas(Long playerId, LocalDate startDate, LocalDate endDate) {
        Specification<PlayerActivityJpa> spec = playerActivitySpec(playerId, startDate, endDate);
        return playerActivityRepository.findBy(
            spec,
            FluentQuery.FetchableFluentQuery::all
        );
    }

    private static Specification<PlayerActivityJpa> playerActivitySpec(Long playerId, LocalDate startDate, LocalDate endDate) {
        return (root, query, builder) ->
            builder.and(
                builder.equal(root.get("playerJpa").get("id"), playerId),
                builder.between(root.get("fetchedAt"), startDate, endDate)
            );
    }

}
