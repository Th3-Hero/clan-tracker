package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.*;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DataRetrievalService {
    private final ConfigService configService;
    private final ClanRepository clanRepository;
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
            configJpa.getPerformanceThresholdBad(),
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
    public ActivityInfo getClanActivityData(@NonNull Long clanId, LocalDateTime startDate, LocalDateTime endDate) {
        ConfigJpa configJpa = configService.getConfigJpa();
        ClanJpa clanJpa = clanRepository.findById(clanId)
            .orElseThrow(() -> new ClanNotFoundException("Failed to find clan with id: %s".formatted(clanId)));

        // if either or both dates are null, use the default range
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now().atStartOfDay();
            startDate = endDate.minusDays(configJpa.getDefaultActivitySummaryDateRange());
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

    public PlayerInfo getPlayerActivity(@NotNull String idOrName, @NonNull LocalDateTime startDate, @NonNull LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Long playerId;
        if (idOrName.matches("\\d+")) {
            playerId = Long.parseLong(idOrName);
        } else {
            List<PlayerJpa> players = searchPlayer(idOrName);
            if (players.isEmpty()) {
                throw new IllegalArgumentException("No player found with the name: %s".formatted(idOrName));
            }
            if (players.size() > 1) {
                String names = players.stream().map(PlayerJpa::getName).collect(Collectors.joining("\n"));
                throw new IllegalArgumentException("Multiple players found with the name: %s\nPlayers found:\n%s".formatted(idOrName, names));
            }
            playerId = players.getFirst().getId();
        }

        List<PlayerActivityJpa> playerActivityJpas = findPlayerActivityJpas(playerId, startDate, endDate);
        if (playerActivityJpas.isEmpty()) {
            throw new EntityNotFoundException("No activity data found for player %s between %s and %s".formatted(playerId, startDate, endDate));
        }

        Long randomsDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalRandomBattles);
        Long skirmishDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalSkirmishBattles);
        Long advancesDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalAdvancesBattles);
        Long clanWarDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalClanWarBattles);

        return new PlayerInfo(
            playerId,
            playerActivityJpas.getFirst().getPlayerJpa().getName(),
            startDate,
            endDate,
            randomsDiff,
            skirmishDiff,
            advancesDiff,
            clanWarDiff
        );
    }

    private List<MemberActivity> createMemberActivityList(LocalDateTime startDate, LocalDateTime endDate, ClanJpa clanJpa) {
        List<MemberActivity> memberActivity = new ArrayList<>();
        for (MemberJpa member : clanJpa.getMembers()) {
            // get all the member activity data for the player within the specified time period
            List<PlayerActivityJpa> memberActivityJpas = findPlayerActivityJpas(member.getPlayerJpa().getId(), startDate, endDate);

            Long randomsDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalRandomBattles);
            Long skirmishDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalSkirmishBattles);
            Long advancesDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalAdvancesBattles);
            Long clanWarDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalClanWarBattles);

            MemberActivity activity = new MemberActivity(
                member.getPlayerJpa().getId(),
                member.getPlayerJpa().getName(),
                member.getRank(),
                member.getClanJpa().getId(),
                member.getJoinedClan(),
                daysInClan(member),
                getLastBattle(memberActivityJpas),
                randomsDiff,
                skirmishDiff,
                advancesDiff,
                clanWarDiff
            );
            memberActivity.add(activity);
        }
        return memberActivity;
    }

    private LocalDateTime getLastBattle(List<PlayerActivityJpa> memberActivityJpas) {
        return memberActivityJpas.stream()
            .map(PlayerActivityJpa::getFetchedAt)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
    }

    private static Long daysInClan(MemberJpa memberJpa) {
        return Duration.between(memberJpa.getJoinedClan(), LocalDateTime.now()).toDays();
    }

    private static Long getDiff(List<PlayerActivityJpa> memberJpas, Function<PlayerActivityJpa, Long> map) {
        List<Long> values = memberJpas.stream()
            .map(map)
            .toList();
        return values.stream().max(Long::compareTo).orElse(0L) - values.stream().min(Long::compareTo).orElse(0L);
    }

    private List<PlayerJpa> searchPlayer(String name) {
        List<PlayerSnapshotJpa> players = playerSnapshotRepository.findByNameContaining(name);
        return players.stream()
            .map(PlayerSnapshotJpa::getPlayerJpa).distinct().toList();
    }

    private List<PlayerActivityJpa> findPlayerActivityJpas(Long playerId, LocalDateTime startDate, LocalDateTime endDate) {
        Specification<PlayerActivityJpa> spec = playerActivitySpec(playerId, startDate, endDate);
        return playerActivityRepository.findBy(
            spec,
            FluentQuery.FetchableFluentQuery::all
        );
    }

    private static Specification<PlayerActivityJpa> playerActivitySpec(Long playerId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, builder) ->
            builder.and(
                builder.equal(root.get("playerJpa").get("id"), playerId),
                builder.between(root.get("fetchedAt"), startDate, endDate)
            );
    }

}
