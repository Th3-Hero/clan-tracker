package com.th3hero.clantracker.lib.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.NonNull;

import java.util.Map;

public record MemberInfo(
    @NonNull String status,
    WargamingError error,
    Map<String, EnrichedPlayer> data
) {
    public record EnrichedPlayer(
        @JsonAlias("account_id") Long accountId,
        @JsonAlias("clan_id") Long clanId,
        @JsonAlias("updated_at") Long updatedAt,
        @JsonAlias("last_battle_time") Long lastBattleTime,
        String nickname,
        Map<String, Battle> statistics
    ) {
        public record Battle(Long battles) { }
    }
}
