package com.example.hackathonbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DispatchRequest {
    @NotBlank private String date;
    @NotBlank private String companion;
    @NotBlank private String concept;
    @NotBlank private String area;

    @Valid
    private Constraints constraints; // ✅ 여기서 Constraints 안에 OpeningHours까지 접근 가능

    private List<Place> places; // 이미 Place DTO 있다면 그대로 사용
}
