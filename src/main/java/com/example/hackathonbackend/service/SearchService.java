package com.example.hackathonbackend.service;

import com.example.hackathonbackend.dto.SearchRequestDto;
import com.example.hackathonbackend.dto.SearchResponseDto;
import com.example.hackathonbackend.model.SearchHistory;
import com.example.hackathonbackend.model.User;
import com.example.hackathonbackend.repository.SearchHistoryRepository;
import com.example.hackathonbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public SearchService(SearchHistoryRepository searchHistoryRepository, UserRepository userRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * 검색 처리 및 DB 저장, 추천 기능
     */
    @Transactional
    public SearchResponseDto searchLocation(SearchRequestDto request) {
        // 1️⃣ 카카오 로그인 사용자 조회
        User user = userRepository.findByKakaoId(request.getKakaoId());
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 2️⃣ 검색 기록 저장
        SearchHistory history = new SearchHistory(
                user,
                request.getQuery(),
                null // locationInfo는 나중에 LocationService 호출 후 채움
        );
        searchHistoryRepository.save(history);

        // 3️⃣ 추천 기능 (최근 검색 키워드 기반)
        List<SearchHistory> recentHistory = searchHistoryRepository
                .findTop10ByUserOrderByCreatedAtDesc(user);

        List<String> recommendedPlaces = new ArrayList<>();
        for (SearchHistory h : recentHistory) {
            if (!h.getKeyword().equals(request.getQuery())) {
                recommendedPlaces.add(h.getKeyword()); // 단순 이전 검색 키워드 추천
            }
        }

        // 4️⃣ ResponseDto 생성
        SearchResponseDto response = new SearchResponseDto();
        response.setKeyword(request.getQuery());
        response.setRecommendedPlaces(recommendedPlaces);
        response.setMessage("검색 완료");

        return response;
    }

}
