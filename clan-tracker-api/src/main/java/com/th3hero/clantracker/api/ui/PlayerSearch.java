package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(
    name = "PlayerSearch",
    description = "Information about the activity of players matching the search criteria during a specific period."
)
public record PlayerSearch(
    @Schema(description = "Starting date of the activity period.") @NonNull LocalDateTime startDate,
    @Schema(description = "Ending date of the activity period.") @NonNull LocalDateTime endDate,
    @Schema(description = "List of player info matching criteria") @NonNull List<PlayerInfo> playerInfo
) { }
