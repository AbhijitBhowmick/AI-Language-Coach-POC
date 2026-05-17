package com.platform.common.exception;

import java.time.LocalDateTime;

public record GlobalErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String errorCode
) {
    public static GlobalErrorResponse of(int status, String error, String message, String errorCode) {
        return new GlobalErrorResponse(LocalDateTime.now(), status, error, message, errorCode);
    }
}
