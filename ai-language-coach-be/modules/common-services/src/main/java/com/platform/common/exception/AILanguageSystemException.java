package com.platform.common.exception;

public class AILanguageSystemException extends RuntimeException {

    private final String errorCode;

    public AILanguageSystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AILanguageSystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
