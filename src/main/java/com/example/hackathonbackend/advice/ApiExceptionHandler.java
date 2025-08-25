package com.example.hackathonbackend.advice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 공통 포맷: { "code": 400/404/422/500, "message": "설명", "details": {...} }
    private static Map<String, Object> err(int code, String message, Map<String, Object> details) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", message);
        m.put("details", details == null ? Map.of() : details);
        return m;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e) {
        int code = e.getStatusCode().value();
        String msg = e.getReason() != null ? e.getReason() : e.getMessage();
        return ResponseEntity.status(code).body(err(code, msg, Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe ->
                details.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(err(400, "validation error", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException e) {
        Map<String, Object> details = new HashMap<>();
        e.getConstraintViolations().forEach(v ->
                details.put(String.valueOf(v.getPropertyPath()), v.getMessage()));
        return ResponseEntity.badRequest().body(err(400, "validation error", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParse(HttpMessageNotReadableException e) {
        String cause = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "invalid json";
        return ResponseEntity.badRequest().body(err(400, "invalid json", Map.of("cause", cause)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(err(400, "bad request", Map.of("cause", e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        // 마지막 안전망
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err(500, "internal error", Map.of("cause", e.getMessage())));
    }
}
