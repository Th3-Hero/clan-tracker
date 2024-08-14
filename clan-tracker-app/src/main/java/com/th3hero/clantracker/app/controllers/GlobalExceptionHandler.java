package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClanNotFoundException.class)
    public void clanNotFoundException(ClanNotFoundException e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
