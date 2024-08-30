package com.th3hero.clantracker.app.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record ClanInfo(
    @NotNull String status,
    @Valid WargamingError error,
    @Valid Map<String, EnrichedClan> data
) implements WargamingResponse {

    public record EnrichedClan(
        @JsonAlias("clan_id") @NotNull Long clanId,
        @NotNull String tag,
        @NotNull @Valid List<BasicPlayer> members
    ) {
        public record BasicPlayer(
            @JsonAlias("account_id") @NotNull Long id,
            @JsonAlias("joined_at") @NotNull Long joinedAt,
            @NotNull String role
        ) { }
    }
}
