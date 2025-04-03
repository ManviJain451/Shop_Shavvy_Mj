package com.shopsavvy.shopshavvy.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEntryExistsException extends RuntimeException {
    public DuplicateEntryExistsException(String message) {
        super(message);
    }
}
