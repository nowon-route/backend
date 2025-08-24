package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.LocationRequestDto;
import com.example.hackathonbackend.dto.LocationResponseDto;
import com.example.hackathonbackend.service.LocationService;
import com.example.hackathonbackend.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final LocationService locationService;
    private final SearchService searchService;

    @GetMapping("/search-location")
    public ResponseEntity<Map<String, Object>> searchAndSave(
            @RequestParam(defaultValue = "0") Long userId,     // 로그인 전: 기본 0
            @RequestParam(defaultValue = "6") int mainCount,   // 메인 표시 개수
            @Valid LocationRequestDto req                      // lat/lng/radius/keyword/type/limit
    ) {
        List<LocationResponseDto> places = locationService.getNearbyPlaces(req);
        Long resultId = searchService.saveSearchResult(userId, places, mainCount);

        int main = Math.min(mainCount, places.size());
        int detail = Math.max(0, places.size() - main);

        return ResponseEntity.ok(Map.of(
                "resultId", resultId,
                "total", places.size(),
                "mainCount", main,
                "detailCount", detail,
                "places", places // 필요 없으면 제거
        ));
    }
}
