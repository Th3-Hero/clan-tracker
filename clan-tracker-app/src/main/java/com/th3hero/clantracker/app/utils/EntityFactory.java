package com.th3hero.clantracker.app.utils;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.wargaming.ClanInfo;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class EntityFactory {

    public static PlayerActivityJpa createPlayerActivityJpa(
        MemberJpa member,
        Map<Long, EnrichedPlayer> enrichedPlayerMap,
        LocalDateTime fetchDateTime,
        LocalDate effectiveDate,
        LocalTime effectiveTime
    ) {
        EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(member.getPlayerJpa().getId());
        Long clanWarAbsoluteBattles = enrichedPlayer.statistics().get("globalmap_absolute").battles();
        Long clanWarMiddleBattles = enrichedPlayer.statistics().get("globalmap_middle").battles();
        Long clanWarChampionBattles = enrichedPlayer.statistics().get("globalmap_champion").battles();
        Long clanWarTotalBattles = clanWarAbsoluteBattles + clanWarMiddleBattles + clanWarChampionBattles;
        return PlayerActivityJpa.create(
            member.getPlayerJpa(),
            fetchDateTime,
            DateUtils.fromTimestamp(enrichedPlayer.lastBattleTime()),
            enrichedPlayer.statistics().get("random").battles(),
            enrichedPlayer.statistics().get("stronghold_skirmish").battles(),
            enrichedPlayer.statistics().get("stronghold_defense").battles(),
            clanWarTotalBattles,
            effectiveDate,
            effectiveTime
        );
    }

    public static PlayerSnapshotJpa createPlayerSnapshotJpa(
        ClanJpa clanJpa,
        MemberJpa member,
        Map<Long, ClanInfo.EnrichedClan.BasicPlayer> basicPlayerMap,
        Map<Long, EnrichedPlayer> enrichedPlayerMap,
        LocalDateTime fetchDateTime,
        LocalDate effectiveDate,
        LocalTime effectiveTime
    ) {
        EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(member.getPlayerJpa().getId());
        ClanInfo.EnrichedClan.BasicPlayer basicPlayer = basicPlayerMap.get(enrichedPlayer.accountId());
        return PlayerSnapshotJpa.create(
            member.getPlayerJpa(),
            fetchDateTime,
            clanJpa,
            enrichedPlayer.nickname(),
            EnumUtils.getEnumIgnoreCase(Rank.class, basicPlayer.role()),
            DateUtils.fromTimestamp(basicPlayer.joinedAt()),
            effectiveDate,
            effectiveTime
        );
    }
}
