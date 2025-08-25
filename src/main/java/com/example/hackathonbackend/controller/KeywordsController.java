package com.example.hackathonbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
public class KeywordsController {

    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    public KeywordsController(@Value("${openai.apiKey}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 프론트 입력: { date, companion, concept, area, constraints:{ openingHours:{start,end}, mustExclude:[], budget } }
     * 응답: 모델이 반환한 JSON (예: searchQueries/placeFilters/categories)
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> in) {
        String date      = str(in.get("date"));
        String companion = str(in.get("companion"));
        String concept   = str(in.get("concept"));
        String area      = str(in.get("area"));

        Map<String, Object> constraints = castMap(in.get("constraints"));
        Map<String, Object> opening     = castMap(constraints.get("openingHours"));
        String start = strDefault(opening.get("start"), "10:00");
        String end   = strDefault(opening.get("end"),   "22:00");
        List<String> mustExclude = castList(constraints.get("mustExclude"));
        if (mustExclude.isEmpty()) mustExclude = List.of("매운");
        String budget = strDefault(constraints.get("budget"), "중간");

        // --- 프롬프트 구성 (실제 개행 사용) ---
        String systemContent = String.join("\n",
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
        );

        String userMain = String.join("\n",
                "날짜: " + date,
                "동행: " + companion,
                "컨셉: " + concept,
                "지역: " + area,
                "제약: " + start + "~" + end + ", " + String.join(" ", mustExclude) + " 음식 불가, 예산 " + budget,
                "주의: JSON만 출력. 코드블록/설명 금지."
        );

        String userGoodBad = String.join("\n",
                "나쁜 예:",
                "```",
                "{ \"searchQueries\": [{\"q\":\"...\"}] }  // 이런 설명/코드블록 금지",
                "```",
                "",
                "좋은 예:",
                "{",
                "  \"searchQueries\": [{\"q\":\"노원구 감성 카페 브런치\", \"intent\":\"cafe\"}],",
                "  \"placeFilters\": {",
                "    \"mustInclude\": [\"카페\"],",
                "    \"mustExclude\": [\"매운\"],",
                "    \"openingHours\": {\"start\": \"10:00\", \"end\": \"22:00\"}",
                "  },",
                "  \"categories\": [\"cafe\",\"walk\",\"night_view\"]",
                "}"
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "temperature", 0.4,
                "max_tokens", 800,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user",   "content", userMain),
                        Map.of("role", "user",   "content", userGoodBad)
                )
        );

        Map<String, Object> resp = webClient.post()
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .flatMap(s -> Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY, "OpenAI error: " + s))))
                .bodyToMono(Map.class)
                .block();

        if (resp == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI empty response");

        // choices[0].message.content 에 JSON 문자열이 들어오는 패턴
        Object choicesObj = resp.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI malformed response");
        }
        Object message = ((Map<?, ?>) choices.get(0)).get("message");
        String content = strFromMap(message, "content");

        Map<String, Object> parsed = parseJsonStrict(content);
        if (parsed.isEmpty()) parsed = parseJsonFromText(content);
        if (parsed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "모델이 유효한 JSON을 반환하지 않았습니다.");
        }
        return parsed; // <- 프론트가 원하는 JSON 스키마 그대로 반환
    }

    // ------------------------ helpers ------------------------

    private String str(Object o) { return o == null ? "" : String.valueOf(o); }
    private String strDefault(Object o, String def) { String s = str(o); return s.isBlank() ? def : s; }
    private String strFromMap(Object o, String key) {
        if (!(o instanceof Map<?, ?> m)) return "";
        Object v = m.get(key);
        return v == null ? "" : String.valueOf(v);
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object o) { return (o instanceof Map<?, ?> m) ? (Map<String, Object>) m : Map.of(); }
    @SuppressWarnings("unchecked")
    private List<String> castList(Object o) { return (o instanceof List<?> l) ? (List<String>) l : List.of(); }

    private Map<String, Object> parseJsonStrict(String s) {
        if (!StringUtils.hasText(s)) return Map.of();
        try { return om.readValue(s, Map.class); }
        catch (Exception ignore) { return Map.of(); }
    }
    private Map<String, Object> parseJsonFromText(String s) {
        if (!StringUtils.hasText(s)) return Map.of();
        int a = s.indexOf('{'), b = s.lastIndexOf('}');
        if (a >= 0 && b > a) {
            return parseJsonStrict(s.substring(a, b + 1));
        }
        return Map.of();
    }
}
