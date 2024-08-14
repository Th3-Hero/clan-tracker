package com.th3hero.clantracker.app;

import com.th3hero.clantracker.jpa.entities.MemberJpa;
import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Test;

class TestStuff {

    @Test
    void test() {
        String r = "recruit";

        var result = EnumUtils.getEnumIgnoreCase(MemberJpa.Rank.class, r);

        var t = 5;
    }
}
