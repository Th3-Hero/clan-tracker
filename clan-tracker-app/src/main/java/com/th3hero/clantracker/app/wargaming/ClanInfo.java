package com.th3hero.clantracker.app.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public record ClanInfo(
    @NonNull String status,
    WargamingError error,
    Map<String, EnrichedClan> data
) {

    public record EnrichedClan(
        @JsonAlias("clan_id") @NonNull Long clanId,
        @NonNull String tag,
        @NonNull List<BasicPlayer> members
    ) {
        public record BasicPlayer(
            @JsonAlias("account_id") @NonNull Long id,
            @JsonAlias("joined_at") @NonNull Long joinedAt,
            @NonNull String role
        ) { }
    }
}
