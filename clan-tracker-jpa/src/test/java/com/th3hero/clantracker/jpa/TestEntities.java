package com.th3hero.clantracker.jpa;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestEntities {

    public static ConfigJpa configJpa() {
        return ConfigJpa.builder()
            .id(1L)
            .memberActivityUpdateInterval(12)
            .performanceThresholdBad(7)
            .performanceThresholdPoor(7)
            .performanceThresholdGood(12)
            .defaultActivitySummaryDateRange(28)
            .build();
    }

    public static ClanJpa clanJpa(int seed) {
        return ClanJpa.builder()
            .id((long) seed)
            .tag("Test Clan%s".formatted(seed))
            .members(new ArrayList<>())
            .build();
    }

    public static PlayerJpa playerJpa(int seed) {
        return PlayerJpa.builder()
            .id((long) seed)
            .name("Test Player%s".formatted(seed))
            .build();
    }

    public static MemberJpa memberJpa(int seed) {
        var clan = clanJpa(seed);
        var player = playerJpa(seed);
        var member = MemberJpa.builder()
            .playerJpa(player)
            .clanJpa(clan)
            .rank(Rank.COMBAT_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed+2))
            .build();
        clan.getMembers().add(member);
        return member;
    }

    public static PlayerActivityJpa playerActivityJpa(int seed) {
        var player = playerJpa(seed);
        return PlayerActivityJpa.builder()
            .playerJpa(player)
            .fetchedAt(LocalDateTime.now().minusDays(seed))
            .lastBattle(LocalDateTime.now().minusDays(seed))
            .totalRandomBattles(101L)
            .totalSkirmishBattles(102L)
            .totalAdvancesBattles(103L)
            .totalClanWarBattles(104L)
            .build();
    }

    public static PlayerSnapshotJpa playerSnapshotJpa(int seed) {
        var clan = clanJpa(seed);
        var player = playerJpa(seed);
        var member = MemberJpa.builder()
            .playerJpa(player)
            .clanJpa(clan)
            .rank(Rank.PERSONNEL_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed+2))
            .build();
        clan.getMembers().add(member);
        return PlayerSnapshotJpa.builder()
            .playerJpa(player)
            .fetchedAt(LocalDateTime.now().minusDays(seed))
            .name(player.getName())
            .clanJpa(clan)
            .rank(Rank.JUNIOR_OFFICER)
            .joinedAt(LocalDateTime.now().minusMonths(seed))
            .build();
    }
}
