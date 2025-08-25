package com.example.hackathonbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class KeywordRequest {
    @NotBlank
    private String date;

    @NotBlank
    private String companion;

    @NotBlank
    private String concept;

    @NotBlank
    private String area;

    // 필요 시: constraints 필드도 추가
    // private Constraints constraints;
}
