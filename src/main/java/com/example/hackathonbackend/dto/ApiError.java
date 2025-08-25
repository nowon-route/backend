package com.example.hackathonbackend.dto;

import java.util.Map;

public record ApiError(
        int code,
        String message,
        Map<String, Object> details
) {}
