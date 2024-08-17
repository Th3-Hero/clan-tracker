package com.th3hero.clantracker.jpa.ui;

import lombok.NonNull;

public record Clan(
    @NonNull Long id,
    @NonNull String tag
) { }
