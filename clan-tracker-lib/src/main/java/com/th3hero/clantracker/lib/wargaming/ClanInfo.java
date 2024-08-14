package com.th3hero.clantracker.lib.wargaming;

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
        @JsonAlias("clan_id") Long clanId,
        String tag,
        List<BasicPlayer> members
    ) {
        public record BasicPlayer(
            @JsonAlias("account_id") Long id,
            @JsonAlias("joined_at") Long joinedAt,
            String role
        ) { }
    }
}
