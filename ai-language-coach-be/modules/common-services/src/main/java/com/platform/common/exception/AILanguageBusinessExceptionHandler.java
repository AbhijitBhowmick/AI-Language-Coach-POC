package com.platform.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AILanguageBusinessExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AILanguageBusinessExceptionHandler.class);

    @ExceptionHandler(AILanguageBusinessException.class)
    public ResponseEntity<GlobalErrorResponse> handleBusinessException(AILanguageBusinessException ex) {
        log.error("Business Error: code={}, status={}, message={}", 
                  ex.getErrorCode(), ex.getHttpStatus(), ex.getMessage());
        
        GlobalErrorResponse response = GlobalErrorResponse.of(
                ex.getHttpStatus(),
                "Business Error",
                ex.getMessage(),
                ex.getErrorCode()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }
}
