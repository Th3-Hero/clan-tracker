package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;

@Schema(
    name = "ActivityInfo",
    description = "Information about the activity of a clan."
)
public record ActivityInfo(
    @Schema(description = "The clan the activity data is for.") @NonNull Clan clan,
    @Schema(description = "Starting date of the activity period.") @NonNull LocalDate startDate,
    @Schema(description = "Ending date of the activity period.") @NonNull LocalDate endDate,
    @Schema(description = "Activity for clan members within the specified time period.") @NonNull List<MemberActivity> memberActivity
) { }
