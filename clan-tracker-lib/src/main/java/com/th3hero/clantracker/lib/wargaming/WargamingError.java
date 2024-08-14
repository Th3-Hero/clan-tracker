package com.th3hero.clantracker.lib.wargaming;

public record WargamingError(
    String message,
    String field,
    String value,
    Long code
) { }
