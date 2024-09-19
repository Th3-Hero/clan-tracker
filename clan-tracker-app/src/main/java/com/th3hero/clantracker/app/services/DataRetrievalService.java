package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.ActivityInfo;
import com.th3hero.clantracker.api.ui.Clan;
import com.th3hero.clantracker.api.ui.Config;
import com.th3hero.clantracker.api.ui.MemberActivity;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityRepository;
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

@Service
@Transactional
@RequiredArgsConstructor
public class DataRetrievalService {
    private final ConfigService configService;
    private final ClanRepository clanRepository;
    private final PlayerActivityRepository playerActivityRepository;

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

    private List<MemberActivity> createMemberActivityList(LocalDateTime startDate, LocalDateTime endDate, ClanJpa clanJpa) {
        List<MemberActivity> memberActivity = new ArrayList<>();
        for (MemberJpa member : clanJpa.getMembers()) {
            // get all the member activity data for the player within the specified time period
            Specification<PlayerActivityJpa> spec = playerActivitySpec(member.getPlayerJpa().getId(), startDate, endDate);
            List<PlayerActivityJpa> memberActivityJpas = playerActivityRepository.findBy(
                spec,
                FluentQuery.FetchableFluentQuery::all
            );

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

    private static Specification<PlayerActivityJpa> playerActivitySpec(Long memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, builder) ->
            builder.and(
                builder.equal(root.get("memberId"), memberId),
                builder.between(root.get("updatedAt"), startDate, endDate)
            );
    }

}
