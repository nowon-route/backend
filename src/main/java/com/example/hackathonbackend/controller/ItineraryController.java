// src/main/java/com/example/hackathonbackend/controller/ItineraryController.java
package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.DispatchRequest;
import com.example.hackathonbackend.dto.KeywordRequest;
import com.example.hackathonbackend.service.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    /**
     * [2번] GPT 키워드 생성 (프론트 호출용)
     * POST /api/itineraries/keywords
     * Request: KeywordRequest (date, companion, concept, area)
     * Response: { searchQueries, placeFilters, categories, _debug? }
     */
    @PostMapping("/keywords")
    public ResponseEntity<Map<String, Object>> keywords(@Valid @RequestBody KeywordRequest req) {
        return ResponseEntity.ok(itineraryService.generate(req)); // 200 OK
    }

    /**
     * [3번] 지도팀 배치 연계 (백엔드 → 지도팀)
     * POST /api/itineraries/keywords/dispatch
     * Request: DispatchRequest (date, companion, concept, area, constraints?, places?)
     * Response: 202 Accepted + ACK payload
     */
    @PostMapping("/keywords/dispatch")
    public ResponseEntity<Map<String, Object>> dispatchToMaps(@Valid @RequestBody DispatchRequest req) {
        Map<String, Object> ack = itineraryService.dispatchToMaps(req);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ack); // 202
    }
}
