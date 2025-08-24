// src/main/java/com/example/hackathonbackend/dto/LocationRequestDto.java
package com.example.hackathonbackend.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class LocationRequestDto {
    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @Min(1) @Max(50000)   // 반경은 최소 1m ~ 최대 50km
    private int radius = 1500;

    private String keyword;

    private String type;

    @Min(1) @Max(50)      // limit은 최소 1, 최대 50개
    private int limit = 20;
}
