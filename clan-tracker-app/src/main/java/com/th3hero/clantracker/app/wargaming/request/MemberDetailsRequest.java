package com.th3hero.clantracker.app.wargaming.request;

import lombok.Builder;

public class MemberDetailsRequest extends RequestBody {
    @Builder
    public MemberDetailsRequest(String token, String memberIds, String fields, String extra) {
        super(token, fields, extra);
        add("account_id", memberIds);
    }
}
