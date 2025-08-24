package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.LocationRequestDto;
import com.example.hackathonbackend.dto.LocationResponseDto;
import com.example.hackathonbackend.dto.SearchRequestDto;
import com.example.hackathonbackend.dto.SearchResponseDto;
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
@RequestMapping("/search-location")
public class SearchController {

    private final LocationService locationService;
    private final SearchService searchService;

    /**
     * GET 요청: 위치 기반 검색 + 검색 결과 저장
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> searchAndSave(
            @RequestParam(defaultValue = "0") Long userId,
            @RequestParam(defaultValue = "6") int mainCount,
            @Valid LocationRequestDto req
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
                "places", places
        ));
    }

    /**
     * POST 요청: 클라이언트 검색 요청 처리
     */
    @PostMapping
    public ResponseEntity<SearchResponseDto> search(@RequestBody SearchRequestDto request) {
        SearchResponseDto response = searchService.searchLocation(request);
        return ResponseEntity.ok(response);
    }
}
