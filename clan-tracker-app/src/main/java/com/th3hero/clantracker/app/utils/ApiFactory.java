package com.th3hero.clantracker.app.utils;

import com.th3hero.clantracker.api.ui.MemberActivity;
import com.th3hero.clantracker.api.ui.PlayerInfo;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ApiFactory {

    public static MemberActivity createMemberActivity(MemberJpa memberJpa, List<PlayerActivityJpa> memberActivityJpas) {
        Long randomsDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalRandomBattles);
        Long skirmishDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalSkirmishBattles);
        Long advancesDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalAdvancesBattles);
        Long clanWarDiff = getDiff(memberActivityJpas, PlayerActivityJpa::getTotalClanWarBattles);

        return new MemberActivity(
            memberJpa.getPlayerJpa().getId(),
            memberJpa.getPlayerJpa().getName(),
            memberJpa.getRank(),
            memberJpa.getClanJpa().getId(),
            memberJpa.getJoinedClan(),
            daysInClan(memberJpa),
            getLastBattle(memberActivityJpas),
            randomsDiff,
            skirmishDiff,
            advancesDiff,
            clanWarDiff
        );
    }

    public static PlayerInfo createPlayerInfo(Long playerId, List<PlayerActivityJpa> playerActivityJpas) {
        Long randomsDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalRandomBattles);
        Long skirmishDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalSkirmishBattles);
        Long advancesDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalAdvancesBattles);
        Long clanWarDiff = getDiff(playerActivityJpas, PlayerActivityJpa::getTotalClanWarBattles);

        return new PlayerInfo(
            playerId,
            playerActivityJpas.getFirst().getPlayerJpa().getName(),
            randomsDiff,
            skirmishDiff,
            advancesDiff,
            clanWarDiff
        );
    }

    private static LocalDateTime getLastBattle(List<PlayerActivityJpa> memberActivityJpas) {
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
}
