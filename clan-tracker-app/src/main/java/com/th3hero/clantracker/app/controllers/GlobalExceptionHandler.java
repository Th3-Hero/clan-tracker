package com.th3hero.clantracker.app.controllers;

import com.kseth.development.rest.error.ProblemDetailFactory;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import jakarta.persistence.EntityExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClanNotFoundException.class)
    public ProblemDetail clanNotFoundException(ClanNotFoundException e) {
        return ProblemDetailFactory.createProblemDetail(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ProblemDetail entityExistsException(EntityExistsException e) {
        return ProblemDetailFactory.createProblemDetail(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail illegalArgumentException(IllegalArgumentException e) {
        return ProblemDetailFactory.createProblemDetail(HttpStatus.BAD_REQUEST, e);
    }
}
