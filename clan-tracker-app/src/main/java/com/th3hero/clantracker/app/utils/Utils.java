package com.th3hero.clantracker.app.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class Utils {

    public static String importRankTransform(String rank) {
        return rank.replace(" ", "_").toLowerCase();
    }
}
