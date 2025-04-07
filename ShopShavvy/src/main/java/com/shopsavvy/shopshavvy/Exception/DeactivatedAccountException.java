package com.shopsavvy.shopshavvy.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class DeactivatedAccountException extends RuntimeException {
    public DeactivatedAccountException(String message) {
        super(message);
    }
}
