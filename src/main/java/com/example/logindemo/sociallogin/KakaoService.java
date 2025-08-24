package com.example.logindemo.sociallogin;

import com.example.logindemo.member.Member;
import com.example.logindemo.member.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class KakaoService {

    private static final Logger log = LoggerFactory.getLogger(KakaoService.class);

    private final MemberRepository memberRepository;
    private final String clientId;
    private final String redirectUri;

    public KakaoService(
            MemberRepository memberRepository,
            @Value("${kakao.client_id}") String clientId,
            @Value("${kakao.redirect_uri}") String redirectUri
    ) {
        this.memberRepository = memberRepository;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String getAccessTokenFromKakao(String code) {
        log.info("카카오 토큰 요청 시작 - code: {}", code);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT_CHARSET, "utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        log.info("토큰 요청 파라미터 - client_id: {}, redirect_uri: {}", clientId, redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        ResponseEntity<KakaoTokenResponseDto> response =
                restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponseDto.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("카카오 토큰 발급 실패 - status=" + response.getStatusCode());
        }

        KakaoTokenResponseDto body = response.getBody();
        if (body == null || !StringUtils.hasText(body.getAccessToken())) {
            throw new IllegalStateException("카카오 토큰 응답이 비었거나 accessToken 없음");
        }

        // 토큰 전체를 로그에 남기지 않도록 마스킹
        String token = body.getAccessToken();
        String masked = token.length() > 8 ? token.substring(0, 6) + "***" : "***";
        log.info("카카오 토큰 발급 성공 - accessToken(prefix)={}", masked);

        return token;
    }

    public Member saveOrGetMemberFromKakao(String accessToken) {
        KakaoUserInfoResponseDto userInfo = getKakaoUserInfo(accessToken);

        Long kakaoId = userInfo.getId();
        KakaoUserInfoResponseDto.KakaoAccount account = userInfo.getKakaoAccount();

        String email = (account != null) ? account.getEmail() : null;
        String nickname = (account != null && account.getProfile() != null)
                ? account.getProfile().getNickname()
                : null;

        // 프로필 이미지 URL (원본/썸네일 중 우선 사용)
        String profileImage = (account != null && account.getProfile() != null)
                ? account.getProfile().getProfileImageUrl()
                : null;
        String thumbnailImage = (account != null && account.getProfile() != null)
                ? account.getProfile().getThumbnailImageUrl()
                : null;
        String finalImage = StringUtils.hasText(profileImage) ? profileImage : thumbnailImage;

        log.info("카카오 로그인 정보: kakaoId={}, email={}, nickname={}, hasProfileImage={}",
                kakaoId, email, nickname, StringUtils.hasText(finalImage));

        if (email == null) {
            throw new IllegalStateException("카카오 계정에 이메일이 없습니다. (카카오 개발자 콘솔에서 '이메일' 동의 항목 활성화 필요)");
        }

        // 기존 회원이면 닉네임/프로필 이미지 최신화
        Optional<Member> optional = memberRepository.findByUsername(email);
        if (optional.isPresent()) {
            Member existing = optional.get();
            if (StringUtils.hasText(nickname)) {
                existing.setDisplayName(nickname);
            }
            if (StringUtils.hasText(finalImage)) {
                existing.setProfileImageUrl(finalImage);
            }
            return memberRepository.save(existing);
        }

        // 신규 회원 생성
        Member newMember = new Member();
        newMember.setUsername(email);
        newMember.setPassword(UUID.randomUUID().toString());
        newMember.setDisplayName(StringUtils.hasText(nickname) ? nickname : "kakao-user");
        newMember.setProfileImageUrl(finalImage); // ⭐ 프로필 이미지 저장

        return memberRepository.save(newMember);
    }

    public KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String userUrl = "https://kapi.kakao.com/v2/user/me";

        ResponseEntity<KakaoUserInfoResponseDto> response =
                restTemplate.exchange(userUrl, HttpMethod.GET, entity, KakaoUserInfoResponseDto.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회 실패 - status=" + response.getStatusCode());
        }

        return response.getBody();
    }

    @PostConstruct
    public void checkKakaoProps() {
        log.info("Kakao redirectUri = {}", redirectUri);
        log.info("Kakao clientId prefix = {}***",
                clientId != null && clientId.length() >= 6 ? clientId.substring(0, 6) : clientId);
    }
}
