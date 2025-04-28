package com.th3hero.clantracker.app.wargaming.request;

import lombok.NonNull;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Set;

/**
 * Base class for Wargaming API request bodies. Extends {@link LinkedMultiValueMap} as the API takes
 * post bodies as {@code application/ x-www-form-urlencoded} and Spring already has the necessary converters.
 */
public abstract class RequestBody extends LinkedMultiValueMap<String, Object> {
    private static final String APPLICATION_ID = "application_id";

    protected RequestBody(String token, String fields) {
        super();
        add(APPLICATION_ID, token);
        add("fields", fields);
    }

    protected RequestBody(String token, String fields, String extra) {
        super();
        add(APPLICATION_ID, token);
        add("fields", fields);
        add("extra", extra);
    }

    @Override
    @NonNull
    public String toString() {
        Set<String> keys = keySet();
        StringBuilder debugInfo = new StringBuilder();
        for (String key : keys) {
            if (key.equals(APPLICATION_ID)) {
                continue;
            }
            debugInfo.append(key).append("=").append(getFirst(key)).append(", ");
        }
        return debugInfo.toString();
    }
}