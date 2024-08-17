package com.th3hero.clantracker.app;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.dto.config.ConfigUpload;
import com.th3hero.clantracker.jpa.entities.ClanJpa;
import com.th3hero.clantracker.jpa.entities.ConfigJpa;
import com.th3hero.clantracker.jpa.entities.MemberActivityJpa;
import com.th3hero.clantracker.jpa.entities.MemberJpa;

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

    public static MemberJpa memberJpa(int seed) {
        var clan = clanJpa(1);
        var member = MemberJpa.builder()
            .id((long) seed)
            .name("Test Member%s".formatted(seed))
            .clanJpa(clan)
            .rank(Rank.JUNIOR_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed))
            .lastUpdated(LocalDateTime.now())
            .build();
        clan.getMembers().add(member);
        return member;
    }

    public static MemberJpa memberJpa(int seed, ClanJpa clanJpa) {
        var member = MemberJpa.builder()
            .id((long) seed)
            .name("Test Member%s".formatted(seed))
            .clanJpa(clanJpa)
            .rank(Rank.JUNIOR_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed))
            .lastUpdated(LocalDateTime.now())
            .build();
        clanJpa.getMembers().add(member);
        return member;
    }

    public static MemberActivityJpa memberActivityJpa(int seed) {
        var member = memberJpa(1);
        return MemberActivityJpa.builder()
            .memberId(member.getId())
            .updatedAt(LocalDateTime.now().minusDays(seed))
            .name(member.getName())
            .rank(member.getRank())
            .clanId(member.getClanJpa().getId())
            .joinedClan(member.getJoinedClan())
            .lastBattle(LocalDateTime.now().minusDays(seed))
            .totalRandomBattles(100L)
            .totalSkirmishBattles(100L)
            .build();
    }

    public static ConfigUpload configUpload() {
        return new ConfigUpload(28, 28, 20, 30, 40);
    }
}
