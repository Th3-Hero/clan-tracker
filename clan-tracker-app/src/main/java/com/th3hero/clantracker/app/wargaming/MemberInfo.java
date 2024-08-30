package com.th3hero.clantracker.app.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MemberInfo(
    @NotNull String status,
    @Valid WargamingError error,
    @Valid Map<String, EnrichedPlayer> data
) implements WargamingResponse {
    public record EnrichedPlayer(
        @JsonAlias("account_id") @NotNull Long accountId,
        @JsonAlias("updated_at") @NotNull Long updatedAt,
        @JsonAlias("last_battle_time") @NotNull Long lastBattleTime,
        @NotNull String nickname,
        @NotNull @Valid Map<String, Battle> statistics
    ) {
        public record Battle(@NotNull Long battles) { }
    }
}
