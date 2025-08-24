package com.example.logindemo.controller;

import com.example.logindemo.member.Member;
import com.example.logindemo.sociallogin.KakaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/OAuth2")
public class KakaoController {

    private final KakaoService kakaoService;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    public KakaoController(KakaoService kakaoService) {
        this.kakaoService = kakaoService;
    }

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> loginPage() {
        String location = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, location)
                .build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpSession session) {
        // 1) 인가코드로 액세스 토큰 발급
        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        // 2) 사용자 정보 조회/저장
        Member member = kakaoService.saveOrGetMemberFromKakao(accessToken);

        // 3) 세션 저장 (키: loggedInMember)
        session.setAttribute("loggedInMember", member);
        session.setMaxInactiveInterval(60 * 60 * 3); // 3시간

        // 4) 마이페이지로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/MyPage")
                .build();
    }
}
