package com.th3hero.clantracker.jpa.ui;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityInfo(
    @NonNull Clan clan,
    @NonNull LocalDateTime startDate,
    @NonNull LocalDateTime endDate,
    @NonNull Integer performanceThresholdBad,
    @NonNull Integer performanceThresholdPoor,
    @NonNull Integer performanceThresholdGood,
    @NonNull List<MemberActivity> memberActivity
) { }
