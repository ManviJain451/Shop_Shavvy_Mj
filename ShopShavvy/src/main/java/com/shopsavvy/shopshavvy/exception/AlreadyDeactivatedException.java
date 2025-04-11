package com.shopsavvy.shopshavvy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class AlreadyDeactivatedException extends RuntimeException {
    public AlreadyDeactivatedException(String message) {
        super(message);
    }
}
