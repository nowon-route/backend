// src/main/java/com/example/hackathonbackend/service/ItineraryService.java
package com.example.hackathonbackend.service;

import com.example.hackathonbackend.dto.DispatchRequest;
import com.example.hackathonbackend.dto.KeywordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final WebClient openai; // baseUrl=https://api.openai.com/v1, Authorization 헤더 설정되어 있어야 함

    /** 2번: GPT 키워드 생성 */
    public Map<String, Object> generate(KeywordRequest req) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "gpt-3.5-turbo",
                    "temperature", 0.4,
                    "max_tokens", 800,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", String.join("\n",
                                            "너는 여행 키워드 생성기다.",
                                            "반드시 아래 스키마의 JSON만 출력하라. 설명/코드블록/추가 텍스트 금지.",
                                            "스키마:",
                                            "{",
                                            "  \"searchQueries\": [ {\"q\": \"string\", \"intent\": \"string\", \"notes\": \"string(optional)\"} ],",
                                            "  \"placeFilters\": {",
                                            "    \"mustInclude\": [\"string\"],",
                                            "    \"mustExclude\": [\"string\"],",
                                            "    \"openingHours\": {\"start\": \"HH:mm\", \"end\": \"HH:mm\"}",
                                            "  },",
                                            "  \"categories\": [\"string\"]",
                                            "}"
                                    )
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", String.format(
                                            "area=%s, date=%s, companion=%s, concept=%s",
                                            nullToEmpty(req.getArea()),
                                            nullToEmpty(req.getDate()),
                                            nullToEmpty(req.getCompanion()),
                                            nullToEmpty(req.getConcept())
                                    )
                            )
                    )
            );

            Map<?, ?> res = openai.post()
                    .uri("/chat/completions") // baseUrl에 /v1 포함되어 있어야 함
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // TODO: res에서 assistant 메시지(content)를 꺼내 JSON 파싱 → Map<String, Object>로 변환
            // 일단 라우팅/호출 확인용 임시 값 반환
            return Map.of(
                    "searchQueries", List.of(
                            Map.of("q", (nullToEmpty(req.getArea()) + " " + nullToEmpty(req.getConcept())).trim(), "intent", "mixed")
                    ),
                    "placeFilters", Map.of(
                            "mustInclude", List.of("카페"),
                            "mustExclude", List.of("매운")
                    ),
                    "categories", List.of("cafe", "night_view"),
                    "_debug", Map.of("openaiRaw", res)
            );

        } catch (WebClientResponseException e) {
            throw new RuntimeException("OpenAI HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI call failed: " + e.getMessage(), e);
        }
    }

    /** 3번: 지도팀 배치 연계(백엔드 → 지도팀) - ACK 형태의 202 응답용 */
    public Map<String, Object> dispatchToMaps(DispatchRequest req) {
        Map<String, Object> echo = new HashMap<>();

        echo.put("date", req.getDate());
        echo.put("companion", req.getCompanion());
        echo.put("concept", req.getConcept());
        echo.put("area", req.getArea());

        // constraints가 있으면 펼쳐 담기
        if (req.getConstraints() != null) {
            Map<String, Object> constraints = new HashMap<>();
            if (req.getConstraints().getOpeningHours() != null) {
                constraints.put("openingHours", Map.of(
                        "start", req.getConstraints().getOpeningHours().getStart(),
                        "end", req.getConstraints().getOpeningHours().getEnd()
                ));
            }
            if (req.getConstraints().getMustInclude() != null) {
                constraints.put("mustInclude", req.getConstraints().getMustInclude());
            }
            if (req.getConstraints().getMustExclude() != null) {
                constraints.put("mustExclude", req.getConstraints().getMustExclude());
            }
            if (!constraints.isEmpty()) {
                echo.put("constraints", constraints);
            }
        }

        // places가 넘어오면 개수만 기록 (상세 매핑은 지도팀 스펙에 맞춰 추가 가능)
        if (req.getPlaces() != null) {
            echo.put("placesCount", req.getPlaces().size());
        }

        // 실제로는 여기서 메시지큐/내부 API 호출 등으로 지도팀에 전달하는 로직이 들어감.
        // 지금은 202 ACK 스펙 맞춘 응답만 반환.
        return Map.of(
                "status", "QUEUED",
                "message", "Dispatch request accepted and queued for maps team.",
                "echo", echo
        );
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
