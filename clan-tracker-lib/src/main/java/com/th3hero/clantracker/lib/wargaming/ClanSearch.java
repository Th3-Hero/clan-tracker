package com.th3hero.clantracker.lib.wargaming;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.NonNull;

import java.util.List;

public record ClanSearch(
    @NonNull String status,
    WargamingError error,
    List<BasicClan> data
) {
    public record BasicClan(@JsonAlias("clan_id") @NonNull Long id, @NonNull String tag) { }
}
