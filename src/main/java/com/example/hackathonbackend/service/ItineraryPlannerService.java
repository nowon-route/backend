// src/main/java/com/example/hackathonbackend/service/ItineraryPlannerService.java
package com.example.hackathonbackend.service;

import com.example.hackathonbackend.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

// ItineraryPlannerService.java (발췌/교체용)
@Service
@RequiredArgsConstructor
public class ItineraryPlannerService {

    private final WebClient openai;           // util.HttpConfig 에서 주입
    private final ObjectMapper om = new ObjectMapper();

    public ItineraryResponse plan(PlanRequest req) {
        if (req.getPlaces() == null || req.getPlaces().isEmpty()) {
            throw new IllegalArgumentException("places가 비어 있습니다.");
        }

        String system = buildSystemPrompt();
        String user   = buildUserPrompt(req);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo"); // 팀 합의 유지
        body.put("temperature", 0.2);
        body.put("max_tokens", 1500);       // ← 여유 확보
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user",   "content", user)
        ));

        // 3.5-turbo 에서는 json_object 지원(세대에 따라 다름) → 시도, 실패하면 무시해도 됨
        body.put("response_format", Map.of("type","json_object"));

        String raw = callOpenAI(body);                  // ① API 호출
        String json = sanitizeToJson(raw);              // ② JSON만 추출
        try {
            // DTO 바인딩
            ItineraryResponse resp = om.readValue(json, ItineraryResponse.class);
            // places로부터 필드 복사 보정(모델 누락 대비)
            copyPlaceFieldsFromInput(req, resp);
            return resp;
        } catch (Exception e) {
            // 디버깅에 도움되도록 앞부분 샘플 포함
            String head = raw == null ? "null" : raw.substring(0, Math.min(500, raw.length()));
            throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + e.getMessage() +
                    " | raw(head): " + head);
        }
    }

    private String callOpenAI(Map<String,Object> payload) {
        try {
            JsonNode res = openai.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // choices[0].message.content
            String content = res.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
            }
            return content;
        } catch (WebClientResponseException e) {
            // 4xx/5xx 응답 바디 그대로 노출
            throw new RuntimeException("OpenAI 호출 실패: " + e.getRawStatusCode() +
                    " body=" + e.getResponseBodyAsString(), e);
        }
    }

    /** 모델이 설명문/코드블럭을 섞어도 JSON 부분만 안전하게 추출 */
    private String sanitizeToJson(String raw) {
        try {
            String s = raw == null ? "" : raw.trim();

            // 1) 엔드 마커 잘라내기
            int endMarker = s.indexOf("<END_JSON>");
            if (endMarker >= 0) s = s.substring(0, endMarker);

            // 2) 코드펜스 제거 ```json ... ``` 또는 ``` ...
            if (s.startsWith("```")) {
                int first = s.indexOf('\n');
                int last  = s.lastIndexOf("```");
                if (first > 0 && last > first) {
                    s = s.substring(first + 1, last).trim();
                }
            }

            // 3) 후보 JSON 조각 추출: (a) 객체 {...} (b) 배열 [...]
            List<String> candidates = new ArrayList<>();
            candidates.addAll(extractBalancedBlocks(s, '{', '}'));
            candidates.addAll(extractBalancedBlocks(s, '[', ']'));

            // 4) 길이 긴 순으로 시도 → 유효한 것 반환
            candidates.sort(Comparator.comparingInt(String::length).reversed());
            for (String c : candidates) {
                try {
                    om.readTree(c);   // 유효성 검사
                    return c;
                } catch (Exception ignore) {
                    // 다음 후보 계속
                }
            }

            // 5) 모두 실패 → 에러
            String head = s.substring(0, Math.min(400, s.length()));
            String tail = s.length() > 800 ? s.substring(s.length()-400) : "";
            throw new RuntimeException("JSON 유효한 블록을 찾지 못했습니다. head="
                    + head + (tail.isEmpty() ? "" : " ... tail=" + tail));
        } catch (Exception ex) {
            // 체크예외 방지: 항상 런타임으로 래핑
            throw new RuntimeException("JSON 유효성 검사 실패: " + ex.getMessage(), ex);
        }
    }

    /**
     * 문자열에서 주어진 여닫는 기호로 '정상적으로 닫힌' 블록들을 모두 찾아 반환.
     * - 문자열/이스케이프 처리 고려
     * - 중첩 허용
     */
    private List<String> extractBalancedBlocks(String s, char open, char close) {
        List<String> out = new ArrayList<>();
        boolean inString = false;
        boolean escaping = false;
        int depth = 0;
        int start = -1;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false; // 이스케이프 다음 문자 소모
                } else {
                    if (ch == '\\') escaping = true;
                    else if (ch == '"') inString = false;
                }
                continue;
            } else {
                if (ch == '"') {
                    inString = true;
                    continue;
                }
            }

            if (ch == open) {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == close) {
                if (depth > 0) depth--;
                if (depth == 0 && start >= 0) {
                    out.add(s.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return out;
    }

    /** 시스템 프롬프트: JSON-only + 복사 규칙 + 엔드마커 */
    private String buildSystemPrompt() {
        return String.join("\n",
                "너는 여행 코스 플래너다.",
                "반드시 아래 스키마의 JSON만 출력하라. 코드블록, 주석, 설명문 금지.",
                "출력 마지막에는 반드시 `<END_JSON>` 마커를 붙여라.",
                "",
                // ⚠️ 모델에 긴 필드 금지: 서버가 나중에 채운다고 명시
                "주소/좌표/평점/리뷰수/사진 URL 등 긴 필드는 응답에 넣지 마라. 빈 문자열(\"\"), 0 또는 생략해도 된다.",
                "서버가 동일 googlePlaceId 기준으로 address/lat/lng/photoUrl/rating/userRatingsTotal를 나중에 채운다.",
                "",
                "스키마:",
                "{",
                "  \"area\": \"string\",",
                "  \"date\": \"YYYY-MM-DD\",",
                "  \"companion\": \"string\",",
                "  \"concept\": \"string\",",
                "  \"itinerary\": [",
                "    {",
                "      \"order\": 1,",
                "      \"googlePlaceId\": \"string\",",
                "      \"name\": \"string\",",
                "      \"category\": \"string\",",
                "      \"address\": \"string(optional)\",",
                "      \"lat\": 0.0,",
                "      \"lng\": 0.0,",
                "      \"photoUrl\": \"string(optional)\",",
                "      \"rating\": 0.0,",
                "      \"userRatingsTotal\": 0,",
                "      \"arriveAt\": \"HH:mm\",",
                "      \"stayMinutes\": 0,",
                "      \"notes\": \"string(optional)\",",
                "      \"costHint\": \"string(optional)\"",
                "    }",
                "  ],",
                "  \"warnings\": [\"string\"],",
                "  \"reasons\": [\"string\"]",
                "}",
                "",
                // 출력 길이 제한(잘림 방지)
                "규칙:",
                "- itinerary는 최대 4개 스탑만 포함한다.",
                "- 각 필드 값은 가능한 한 짧게 쓴다.",
                "- photoUrl은 절대 넣지 말고 빈 문자열(\"\")로 둔다.",
                "<END_JSON>"
        );
    }


    /** 사용자 프롬프트(places를 그대로 내려 보냄) */
    private String buildUserPrompt(PlanRequest req) {
        try {
            Map<String, Object> u = new LinkedHashMap<>();
            u.put("area", req.getArea());
            u.put("date", req.getDate());
            u.put("companion", req.getCompanion());
            u.put("concept", req.getConcept());
            u.put("constraints", req.getConstraints());

            // places를 슬림화: ID/이름/카테고리/평점만, 최대 8개 정도로 제한
            List<Map<String, Object>> slimPlaces = Optional.ofNullable(req.getPlaces()).orElse(List.of())
                    .stream()
                    .limit(8) // 과하면 토큰 폭증 → 상한 둠
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("googlePlaceId", p.getGooglePlaceId());
                        m.put("name", p.getName());
                        m.put("category", p.getCategory());
                        if (p.getRating() != null) m.put("rating", p.getRating());
                        return m;
                    })
                    .toList();

            u.put("places", slimPlaces);
            return om.writeValueAsString(u);
        } catch (Exception e) {
            throw new IllegalArgumentException("user 프롬프트 생성 실패: " + e.getMessage(), e);
        }
    }

    /** 모델 누락 대비로, 입력 places에서 보정 복사 */
    private void copyPlaceFieldsFromInput(PlanRequest req, ItineraryResponse resp) {
        if (resp == null || resp.getItinerary() == null) return;
        Map<String, Place> byId = Optional.ofNullable(req.getPlaces()).orElse(List.of())
                .stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(Place::getGooglePlaceId, p -> p, (a,b)->a));

        for (ItineraryStop s : resp.getItinerary()) {
            if (s == null || s.getGooglePlaceId() == null) continue;
            Place src = byId.get(s.getGooglePlaceId());
            if (src == null) continue;

            s.setAddress(src.getAddress());
            s.setLat(src.getLat());
            s.setLng(src.getLng());
            s.setPhotoUrl(src.getPhotoUrl());
            s.setRating(src.getRating() == null ? 0.0 : src.getRating());
            s.setUserRatingsTotal(src.getUserRatingsTotal() == null ? 0 : src.getUserRatingsTotal());
        }
    }
}
