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
        @JsonAlias("account_id") @NonNull Long accountId,
        @JsonAlias("clan_id") @NonNull Long clanId,
        @JsonAlias("updated_at") @NonNull Long updatedAt,
        @JsonAlias("last_battle_time") @NonNull Long lastBattleTime,
        @NonNull String nickname,
        @NonNull Map<String, Battle> statistics
    ) {
        public record Battle(@NonNull Long battles) { }
    }
}
