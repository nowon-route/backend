package com.example.hackathonbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItineraryRequest {

    @NotBlank
    private String date;        // "YYYY-MM-DD"

    @NotBlank
    private String companion;   // "연인", "친구" 등

    @NotBlank
    private String concept;     // "감성 카페 + 야경"

    // 선택: 클라이언트가 보낼 수도 있는 지역
    private String area;
}
