package com.th3hero.clantracker.app.wargaming.request;

import lombok.Builder;

public class ClanDetailsRequest extends RequestBody {
    @Builder
    public ClanDetailsRequest(String token, String fields, String clanId) {
        super(token, fields);
        add("clan_id", clanId);
    }
}
