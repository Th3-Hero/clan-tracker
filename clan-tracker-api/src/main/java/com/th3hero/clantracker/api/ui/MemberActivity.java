package com.th3hero.clantracker.api.ui;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import java.time.LocalDateTime;

@Schema(
    name = "MemberActivity",
    description = "Information about the activity of a clan member during a specific period."
)
public record MemberActivity(
    @Schema(description = "Wargaming id of the clan member(clan tracker API also uses this).") @NonNull Long memberId,
    @Schema(description = "Wargaming account name of the member.") @NonNull String name,
    @Schema(description = "Members rank within the clan.") @NonNull Rank rank,
    @Schema(description = "Id for the clan they are apart of.") @NonNull Long clanId,
    @Schema(description = "When the member joined the clan.") @NonNull LocalDateTime joinedClan,
    @Schema(description = "How long the member has been in the clan.") @NonNull Long daysInClan,
    @Schema(description = "When the member last played a match.") @NonNull LocalDateTime lastBattle,
    @Schema(description = "How many random battles the member has played within the time period.") @NonNull Long randomsDiff,
    @Schema(description = "How many skirmish battles the member has played within the time period.") @NonNull Long skirmishDiff,
    @Schema(description = "How many advances the member has played within the time period.") @NonNull Long advancesDiff,
    @Schema(description = "How many clan wars battles the member has played within the time period.") @NonNull Long clanWarDiff
) { }
