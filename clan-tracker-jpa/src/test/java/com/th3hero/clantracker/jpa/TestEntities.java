package com.th3hero.clantracker.jpa;

import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.config.ConfigJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;

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
}
