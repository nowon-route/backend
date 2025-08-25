// src/main/java/com/example/hackathonbackend/dto/PlanRequest.java
package com.example.hackathonbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter; import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PlanRequest {
    @NotBlank private String date;        // "YYYY-MM-DD"
    @NotBlank private String companion;   // "연인"
    @NotBlank private String concept;     // "감성 카페 + 야경"
    @NotBlank private String area;        // "서울 노원구"

    private List<String> intentBundle;    // ["cafe","night_view"]
    private Constraints constraints;      // 운영시간/제약/예산
    private List<Place> places;           // 지도 API가 준 장소들(반드시 이 안에서만 사용)
}
