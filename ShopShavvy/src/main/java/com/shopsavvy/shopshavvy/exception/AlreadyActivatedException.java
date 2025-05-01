package com.shopsavvy.shopshavvy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.OK)
public class AlreadyActivatedException extends RuntimeException {
    public AlreadyActivatedException(String message) {
        super(message);
    }
}
