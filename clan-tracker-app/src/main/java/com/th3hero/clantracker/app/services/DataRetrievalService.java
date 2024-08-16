package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.dto.ui.ActivityInfo;
import com.th3hero.clantracker.app.dto.ui.Clan;
import com.th3hero.clantracker.app.dto.ui.MemberActivity;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.jpa.entities.ClanJpa;
import com.th3hero.clantracker.jpa.entities.ConfigJpa;
import com.th3hero.clantracker.jpa.entities.MemberActivityJpa;
import com.th3hero.clantracker.jpa.entities.MemberJpa;
import com.th3hero.clantracker.jpa.repositories.ClanRepository;
import com.th3hero.clantracker.jpa.repositories.MemberActivityRepository;
import com.th3hero.clantracker.jpa.repositories.MemberRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final MemberRepository memberRepository;
    private final MemberActivityRepository memberActivityRepository;

    public Integer getDefaultActivitySummaryDateRange() {
        return configService.getConfigJpa().getDefaultActivitySummaryDateRange();
    }

    public List<Clan> getClanList() {
        return clanRepository.findAll().stream()
            .map(clanJpa -> new Clan(clanJpa.getId(), clanJpa.getTag()))
            .toList();
    }

    public ActivityInfo getClanActivityData(@NonNull Long clanId, LocalDateTime startDate, LocalDateTime endDate) {
        ConfigJpa configJpa = configService.getConfigJpa();
        ClanJpa clanJpa = clanRepository.findById(clanId)
            .orElseThrow(() -> new ClanNotFoundException("Failed to find clan with id: %s".formatted(clanId)));

        if (startDate == null || endDate == null) {
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(configJpa.getDefaultActivitySummaryDateRange());
        } else if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<MemberActivity> memberActivity = new ArrayList<>();
        for (MemberJpa member : clanJpa.getMembers()) {
            Specification<MemberActivityJpa> spec = memberActivitySpec(member.getId(), startDate, endDate);
            List<MemberActivityJpa> memberActivityJpas = memberActivityRepository.findBy(
                spec,
                FluentQuery.FetchableFluentQuery::all
            );

            Long randomsDiff = getDiff(memberActivityJpas, MemberActivityJpa::getTotalRandomBattles);
            Long skirmishDiff = getDiff(memberActivityJpas, MemberActivityJpa::getTotalSkirmishBattles);
            Long advancesDiff = getDiff(memberActivityJpas, MemberActivityJpa::getTotalAdvancesBattles);
            Long clanWarDiff = getDiff(memberActivityJpas, MemberActivityJpa::getTotalClanWarBattles);
            MemberActivity activity = new MemberActivity(
                member.getId(),
                member.getName(),
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

        return new ActivityInfo(
            new Clan(clanJpa.getId(), clanJpa.getTag()),
            startDate,
            endDate,
            configJpa.getPerformanceThresholdBad(),
            configJpa.getPerformanceThresholdPoor(),
            configJpa.getPerformanceThresholdGood(),
            memberActivity
        );

    }

    private LocalDateTime getLastBattle(List<MemberActivityJpa> memberActivityJpas) {
        return memberActivityJpas.stream()
            .map(MemberActivityJpa::getUpdatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
    }

    private static Long daysInClan(MemberJpa memberJpa) {
        return Duration.between(memberJpa.getJoinedClan(), LocalDateTime.now()).toDays();
    }

    private static Long getDiff(List<MemberActivityJpa> memberJpas, Function<MemberActivityJpa, Long> map) {
        List<Long> values = memberJpas.stream()
            .map(map)
            .toList();
        return values.stream().max(Long::compareTo).orElse(0L) - values.stream().min(Long::compareTo).orElse(0L);
    }

    private static Specification<MemberActivityJpa> memberActivitySpec(Long memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, builder) ->
            builder.and(
                builder.equal(root.get("memberId"), memberId),
                builder.between(root.get("updatedAt"), startDate, endDate)
            );
    }

}
