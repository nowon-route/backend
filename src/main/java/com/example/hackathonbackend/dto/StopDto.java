package com.example.hackathonbackend.dto;

import lombok.Data;

@Data
public class StopDto {
    private Integer order;              // 1,2,3...
    private String googlePlaceId;       // Google Place ID
    private String name;                // 장소명
    private String category;            // cafe/night_view 등
    private String address;             // 주소(선택)
    private Double lat;                 // 위도
    private Double lng;                 // 경도
    private String arriveAt;            // "HH:mm"
    private Integer stayMinutes;        // 체류시간(분)
    private String notes;               // 메모(선택)
    private String costHint;            // 비용 힌트(선택)
    private String photoUrl;            // 사진 URL(선택)
    private Double rating;              // 평점(선택)
    private Integer userRatingsTotal;   // 평점 수(선택)
}
