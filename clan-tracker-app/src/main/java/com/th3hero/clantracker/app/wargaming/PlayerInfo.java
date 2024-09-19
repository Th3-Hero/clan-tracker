package com.th3hero.clantracker.app.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PlayerInfo(
    @NotNull String status,
    @Valid WargamingError error,
    @Valid Map<String, Player> data
) implements WargamingResponse {
    public record Player(
        @JsonAlias("nickname") @NotNull String name,
        @JsonAlias("account_id") @NotNull Long accountId
    ) { }
}
