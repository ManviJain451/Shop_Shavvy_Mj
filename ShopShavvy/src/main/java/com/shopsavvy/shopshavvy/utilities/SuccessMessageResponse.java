package com.shopsavvy.shopshavvy.utilities;

import lombok.Getter;

import java.time.Instant;

@Getter
public class SuccessMessageResponse<T> {
    private final boolean success;
    private final T data;
    private final Instant timestamp;

    private SuccessMessageResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> SuccessMessageResponse<T> success(T data) {
        return new SuccessMessageResponse<>(true, data);
    }

    public static SuccessMessageResponse<Void> success() {
        return new SuccessMessageResponse<>(true, null);
    }
}