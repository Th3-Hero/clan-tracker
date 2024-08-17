package com.th3hero.clantracker.jpa.ui;

import com.th3hero.clantracker.jpa.entities.MemberJpa.Rank;
import lombok.NonNull;

import java.time.LocalDateTime;

public record MemberActivity(
    @NonNull Long memberId,
    @NonNull String name,
    @NonNull Rank rank,
    @NonNull Long clanId,
    @NonNull LocalDateTime joinedClan,
    @NonNull Long daysInClan,
    @NonNull LocalDateTime lastBattle,
    @NonNull Long randomsDiff,
    @NonNull Long skirmishDiff,
    @NonNull Long advancesDiff,
    @NonNull Long clanWarDiff
) { }
