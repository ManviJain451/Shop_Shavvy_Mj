package com.shopsavvy.shopshavvy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class MaximumUploadSizeExceededException extends RuntimeException{
    public MaximumUploadSizeExceededException(String message){
        super();
    }
}


