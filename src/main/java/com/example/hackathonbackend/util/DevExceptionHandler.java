// src/main/java/com/example/hackathonbackend/util/DevExceptionHandler.java
package com.example.hackathonbackend.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@ControllerAdvice
public class DevExceptionHandler {
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<?> webClient(WebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "type", "WebClientResponseException",
                "status", e.getRawStatusCode(),
                "body", e.getResponseBodyAsString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> any(Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "type", e.getClass().getSimpleName(),
                "message", e.getMessage()
        ));
    }
}
