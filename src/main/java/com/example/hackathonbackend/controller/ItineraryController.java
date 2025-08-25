// src/main/java/com/example/hackathonbackend/controller/ItineraryController.java
package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.*;
import com.example.hackathonbackend.service.ItineraryPlannerService; // 기존 키워드 기능 유지
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

    private final ItineraryPlannerService plannerService;     // 신규: plan

    /** [신규] 지도API places + 조건 → GPT 코스 플래너 */
    @PostMapping("/plan")
    public ResponseEntity<ItineraryResponse> plan(@Valid @RequestBody PlanRequest req) {
        return ResponseEntity.ok(plannerService.plan(req));
    }
}
