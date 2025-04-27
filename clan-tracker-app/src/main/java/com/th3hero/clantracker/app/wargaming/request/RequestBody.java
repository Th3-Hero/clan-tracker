package com.th3hero.clantracker.app.wargaming.request;

import org.springframework.util.LinkedMultiValueMap;

import java.util.Set;

/**
 * Base class for Wargaming API request bodies. Extends {@link LinkedMultiValueMap} as the API takes
 * post bodies as {@code application/ x-www-form-urlencoded} and Spring already has the necessary converters.
 */
public abstract class RequestBody extends LinkedMultiValueMap<String, Object> {
    protected RequestBody(String token, String fields) {
        super();
        add("application_id", token);
        add("fields", fields);
    }

    protected RequestBody(String token, String fields, String extra) {
        super();
        add("application_id", token);
        add("fields", fields);
        add("extra", extra);
    }

    public String getDebugInfo() {
        Set<String> keys = keySet();
        StringBuilder debugInfo = new StringBuilder();
        for (String key : keys) {
            if (key.equals("application_id")) {
                continue;
            }
            debugInfo.append(key).append("=").append(getFirst(key)).append(", ");
        }
        return debugInfo.toString();
    }
}