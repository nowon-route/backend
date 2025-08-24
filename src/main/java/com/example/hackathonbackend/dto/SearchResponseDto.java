package com.example.hackathonbackend.dto;

import java.util.List;

public class SearchResponseDto {

    private String keyword;                    // 최종 키워드
    private List<String> recommendedPlaces;   // 추천 장소 리스트
    private String message;                    // 상태 메시지

    // 기본 생성자
    public SearchResponseDto() {}

    public SearchResponseDto(String keyword, List<String> recommendedPlaces, String message) {
        this.keyword = keyword;
        this.recommendedPlaces = recommendedPlaces;
        this.message = message;
    }

    // Getter & Setter
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getRecommendedPlaces() {
        return recommendedPlaces;
    }

    public void setRecommendedPlaces(List<String> recommendedPlaces) {
        this.recommendedPlaces = recommendedPlaces;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
