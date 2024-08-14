package com.th3hero.clantracker.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ConfigUpload",
    description = "Represents the configuration uploaded for the bot. Any fields not provided will use the default value. When updating, any blank fields will not be updated."
)
public record ConfigUpload(
    @Schema(description = "How often member activity data is pulled (In hours)", defaultValue = "12") Integer memberActivityUpdateInterval,
    @Schema(description = "Default threshold for bad performance", defaultValue = "30") Integer performanceThresholdBad,
    @Schema(description = "Default threshold for poor performance", defaultValue = "40") Integer performanceThresholdPoor,
    @Schema(description = "Default threshold for good performance", defaultValue = "50") Integer performanceThresholdGood,
    @Schema(description = "Default activity period", defaultValue = "28") Integer defaultActivitySummaryDateRange
) { }
