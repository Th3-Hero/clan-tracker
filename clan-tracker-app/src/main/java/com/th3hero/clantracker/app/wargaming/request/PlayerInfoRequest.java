package com.th3hero.clantracker.app.wargaming.request;

import lombok.Builder;

public class PlayerInfoRequest extends RequestBody {
    @Builder
    public PlayerInfoRequest(String token, String fields, String playerIds) {
        super(token, fields);
        add("account_id", playerIds);
    }
}
