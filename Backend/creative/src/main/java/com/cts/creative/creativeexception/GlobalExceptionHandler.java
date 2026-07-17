package com.cts.creative.creativeexception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            CreativeNotFoundException.class)
    public ResponseEntity<String> handleNotFound(
            CreativeNotFoundException ex) {

        return ResponseEntity.badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(
            Exception ex) {

        return ResponseEntity.internalServerError()
                .body(ex.getMessage());
    }
}