package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(
    name = "Clan",
    description = "Information about a clan."
)
public record Clan(
    @Schema(description = "Wargaming id of the clan(Also used by clan tracker api).") @NonNull Long id,
    @Schema(description = "The clans tag.") @NonNull String tag
) { }
