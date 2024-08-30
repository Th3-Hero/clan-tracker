package com.th3hero.clantracker.app.wargaming;

import jakarta.validation.constraints.NotNull;


public record WargamingError(
    @NotNull String message,
    @NotNull String field,
    @NotNull String value,
    @NotNull Long code
) { }
