package com.th3hero.clantracker.app.dto.ui;

import lombok.NonNull;

public record Clan(
    @NonNull Long id,
    @NonNull String tag
) { }
