package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.SearchRequestDto;
import com.example.hackathonbackend.dto.SearchResponseDto;
import com.example.hackathonbackend.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search-location")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 클라이언트의 검색 요청 JSON을 받아 SearchService로 처리하고 결과를 JSON으로 반환
     */
    @PostMapping
    public ResponseEntity<SearchResponseDto> search(@RequestBody SearchRequestDto request) {
        SearchResponseDto response = searchService.searchLocation(request);
        return ResponseEntity.ok(response);
    }

}
