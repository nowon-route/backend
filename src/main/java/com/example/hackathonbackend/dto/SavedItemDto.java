package com.example.hackathonbackend.dto;

import java.time.LocalDateTime;

public record SavedItemDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        String badge,
        String meta,
        String link,
        LocalDateTime updatedAt
) {}