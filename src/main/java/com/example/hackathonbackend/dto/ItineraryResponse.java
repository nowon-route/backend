package com.example.hackathonbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItineraryResponse {
    private String area;                // 예: "서울 노원구"
    private String date;                // "YYYY-MM-DD"
    private String companion;           // "연인"
    private String concept;             // "감성 카페 + 야경"
    private List<StopDto> itinerary;    // 스탑 리스트
    private List<String> warnings;      // 경고 메시지
    private List<String> reasons;       // 구성 이유
}
