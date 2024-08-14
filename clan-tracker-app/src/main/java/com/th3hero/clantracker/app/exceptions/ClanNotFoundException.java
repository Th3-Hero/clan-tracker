package com.th3hero.clantracker.app.exceptions;

public class ClanNotFoundException extends RuntimeException {
    public ClanNotFoundException(String message) {
        super(message);
    }
}
