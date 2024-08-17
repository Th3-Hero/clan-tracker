package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(
    name = "Config",
    description = "Default configuration values for the application. Configurations are dynamic, hence the endpoint."
)
public record Config(
    @Schema(description = "Default activity summary range.") @NonNull Integer defaultActivitySummaryDateRange,
    @Schema(description = "Default for what is considered bad performance by a clan member.") @NonNull Integer defaultPerformanceThresholdBad,
    @Schema(description = "Default for what is considered poor performance by a clan member.") @NonNull Integer defaultPerformanceThresholdPoor,
    @Schema(description = "Default for what is considered good performance by a clan member.") @NonNull Integer defaultPerformanceThresholdGood
) { }
