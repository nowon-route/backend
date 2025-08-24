package com.example.hackathonbackend.sociallogin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 API 응답을 매핑하는 DTO
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 응답에 정의되지 않은 필드는 무시
public class KakaoUserInfoResponseDto {

    /** 카카오 고유 사용자 ID */
    private Long id;

    /** 카카오 계정 정보 (이메일, 이름, 프로필) */
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        /** 프로필 객체 (닉네임, 이미지) */
        private Profile profile;

        /** 이메일 (동의 안 하면 null) */
        private String email;

        /** 이름 (동의 안 하면 null) */
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        /** 닉네임 */
        private String nickname;

        /** 프로필 이미지 URL */
        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        /** 썸네일 이미지 URL */
        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;
    }
}