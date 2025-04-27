package com.th3hero.clantracker.app.wargaming.request;

import lombok.Builder;

public class ClanSearchRequest extends RequestBody {
    @Builder
    public ClanSearchRequest(String token, String fields, String clanTag) {
        super(token, fields);
        add("search", clanTag);
    }
}

