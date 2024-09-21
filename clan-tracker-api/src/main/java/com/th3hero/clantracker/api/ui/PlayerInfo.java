package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import java.time.LocalDateTime;

@Schema(
    name = "PlayerInfo",
    description = "Information about the activity of player during a specific period."
)
public record PlayerInfo(
    @Schema(description = "Wargaming id of the clan member(clan tracker API also uses this).") @NonNull Long id,
    @Schema(description = "Wargaming account name of the member.") @NonNull String name,
    @Schema(description = "Starting date of the activity period.") @NonNull LocalDateTime startDate,
    @Schema(description = "Ending date of the activity period.") @NonNull LocalDateTime endDate,
    @Schema(description = "How many random battles the member has played within the time period.") @NonNull Long randomsDiff,
    @Schema(description = "How many skirmish battles the member has played within the time period.") @NonNull Long skirmishDiff,
    @Schema(description = "How many advances the member has played within the time period.") @NonNull Long advancesDiff,
    @Schema(description = "How many clan wars battles the member has played within the time period.") @NonNull Long clanWarDiff
) { }
