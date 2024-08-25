package com.th3hero.clantracker.app.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ConfigUpload",
    description = "Represents the configuration uploaded for the bot. Any fields not provided will use the default value. When updating, any blank fields will not be updated."
)
public record ConfigUpload(
    @Schema(description = "How often member activity data is pulled (In hours)", defaultValue = "12") Integer memberActivityUpdateInterval,
    @Schema(description = "Default threshold for bad performance", defaultValue = "7") Integer performanceThresholdBad,
    @Schema(description = "Default threshold for poor performance", defaultValue = "7") Integer performanceThresholdPoor,
    @Schema(description = "Default threshold for good performance", defaultValue = "12") Integer performanceThresholdGood,
    @Schema(description = "Default activity period", defaultValue = "28") Integer defaultActivitySummaryDateRange
) { }
