package com.th3hero.clantracker.app;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.dto.config.ConfigUpload;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;

import java.time.LocalDateTime;

public class TestEntities {

    public static ConfigJpa configJpa() {
        return ConfigJpa.builder()
            .id(1L)
            .memberActivityUpdateInterval(28)
            .performanceThresholdBad(20)
            .performanceThresholdPoor(30)
            .performanceThresholdGood(40)
            .defaultActivitySummaryDateRange(28)
            .build();
    }

    public static ClanJpa clanJpa(int seed) {
        return ClanJpa.builder()
            .id((long) seed)
            .tag("Test Clan%s".formatted(seed))
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
            .lastUpdated(LocalDateTime.now().minusDays(seed))
            .build();
        clan.getMembers().add(member);
        return member;
    }

    public static MemberJpa memberJpa(int seed, ClanJpa clanJpa) {
        var player = playerJpa(seed);
        var member = MemberJpa.builder()
            .playerJpa(player)
            .clanJpa(clanJpa)
            .rank(Rank.COMBAT_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed+2))
            .lastUpdated(LocalDateTime.now().minusDays(seed))
            .build();
        clanJpa.getMembers().add(member);
        return member;
    }

    public static MemberJpa memberJpa(PlayerJpa playerJpa, int seed, ClanJpa clanJpa) {
        var member = MemberJpa.builder()
            .playerJpa(playerJpa)
            .clanJpa(clanJpa)
            .rank(Rank.COMBAT_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed+2))
            .lastUpdated(LocalDateTime.now().minusDays(seed))
            .build();
        clanJpa.getMembers().add(member);
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
            .lastUpdated(LocalDateTime.now().minusDays(seed))
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

    public static ConfigUpload configUpload() {
        return new ConfigUpload(28, 28, 20, 30, 40);
    }
}
