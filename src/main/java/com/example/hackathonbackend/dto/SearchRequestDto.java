package com.example.hackathonbackend.dto;

public class SearchRequestDto {

    private String kakaoId; // 카카오 로그인 고유 ID
    private String query;   // 검색 키워드

    // 기본 생성자
    public SearchRequestDto() {}

    public SearchRequestDto(String kakaoId, String query) {
        this.kakaoId = kakaoId;
        this.query = query;
    }

    // Getter & Setter
    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
