package com.platform.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class AILanguageSystemExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AILanguageSystemExceptionHandler.class);

    @ExceptionHandler(AILanguageSystemException.class)
    public ResponseEntity<GlobalErrorResponse> handleSystemException(AILanguageSystemException ex) {
        log.error("System Error: code={}, message={}", ex.getErrorCode(), ex.getMessage(), ex);
        
        GlobalErrorResponse response = GlobalErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "System Error",
                ex.getMessage(),
                ex.getErrorCode()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // Custom message for common errors (like IllegalArgumentException, IllegalStateException)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<GlobalErrorResponse> handleCommonErrors(RuntimeException ex) {
        log.error("Common Error: message={}", ex.getMessage(), ex);
        
        GlobalErrorResponse response = GlobalErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters provided.",
                "ERR_COM_400"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Default one for uncommon ones
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleGeneralException(Exception ex) {
        log.error("Uncommon/System Error: message={}", ex.getMessage(), ex);
        
        GlobalErrorResponse response = GlobalErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                "ERR_SYS_000"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
