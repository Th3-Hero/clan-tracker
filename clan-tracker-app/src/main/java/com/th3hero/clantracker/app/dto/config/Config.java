package com.th3hero.clantracker.app.dto.config;

import com.th3hero.clantracker.jpa.entities.ConfigJpa;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(
    name = "Config",
    description = "Represents the configuration returned by the API"
)
public record Config(
    @NonNull Long id,
    @Schema(description = "How often member activity data is pulled (In hours)", defaultValue = "12") @NonNull Integer memberActivityUpdateInterval,
    @Schema(description = "Default threshold for bad performance", defaultValue = "30") @NonNull Integer performanceThresholdBad,
    @Schema(description = "Default threshold for poor performance", defaultValue = "40") @NonNull Integer performanceThresholdPoor,
    @Schema(description = "Default threshold for good performance", defaultValue = "50") @NonNull Integer performanceThresholdGood,
    @Schema(description = "Default activity period", defaultValue = "28") @NonNull Integer defaultActivitySummaryDateRange
) {
    public static Config fromJpa(ConfigJpa configJpa) {
        return new Config(
            configJpa.getId(),
            configJpa.getMemberActivityUpdateInterval(),
            configJpa.getPerformanceThresholdBad(),
            configJpa.getPerformanceThresholdPoor(),
            configJpa.getPerformanceThresholdGood(),
            configJpa.getDefaultActivitySummaryDateRange()
        );
    }
}
