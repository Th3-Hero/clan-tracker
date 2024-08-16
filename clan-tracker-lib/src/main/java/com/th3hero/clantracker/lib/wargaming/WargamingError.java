package com.th3hero.clantracker.lib.wargaming;

import lombok.NonNull;

public record WargamingError(
    @NonNull String message,
    @NonNull String field,
    @NonNull String value,
    @NonNull Long code
) { }
