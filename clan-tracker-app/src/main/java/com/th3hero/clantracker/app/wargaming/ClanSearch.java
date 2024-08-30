package com.th3hero.clantracker.app.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClanSearch(
    @NotNull String status,
    @Valid WargamingError error,
    @Valid List<BasicClan> data
) {
    public record BasicClan(@JsonAlias("clan_id") @NotNull Long id, @NotNull String tag) { }
}
