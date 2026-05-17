package com.platform.common.exception;

public class AILanguageBusinessException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;

    public AILanguageBusinessException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AILanguageBusinessException(String message, String errorCode) {
        this(message, errorCode, 400); // Default to 400 Bad Request
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
