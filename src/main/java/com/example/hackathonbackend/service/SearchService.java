package com.example.hackathonbackend.service;

import com.example.hackathonbackend.dto.LocationResponseDto;
import com.example.hackathonbackend.dto.SearchRequestDto;
import com.example.hackathonbackend.dto.SearchResponseDto;
import com.example.hackathonbackend.model.Location;
import com.example.hackathonbackend.model.PlaceResult;
import com.example.hackathonbackend.model.PlaceResultItem;
import com.example.hackathonbackend.model.SearchHistory;
import com.example.hackathonbackend.model.User;
import com.example.hackathonbackend.repository.LocationRepository;
import com.example.hackathonbackend.repository.PlaceResultItemRepository;
import com.example.hackathonbackend.repository.PlaceResultRepository;
import com.example.hackathonbackend.repository.SearchHistoryRepository;
import com.example.hackathonbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final long SYSTEM_USER_ID = 0L; // 로그인 전 임시 유저

    private final LocationRepository locationRepository;
    private final PlaceResultRepository placeResultRepository;
    private final PlaceResultItemRepository placeResultItemRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    // 문자열 안전 트림
    private static String safeTrim(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    // Double 널 → 기본값
    private static Double safeDouble(Double v, double fallback) {
        return v != null ? v : fallback;
    }

    /**
     * 장소 검색 결과 저장 (places upsert + place_results 1줄 + place_result_items)
     */
    @Transactional
    public Long saveSearchResult(Long userId, List<LocationResponseDto> places, int mainCount) {
        if (userId == null) userId = SYSTEM_USER_ID;

        PlaceResult result = placeResultRepository.save(
                PlaceResult.builder().userId(userId).build()
        );

        int rank = 1;
        for (int i = 0; i < places.size(); i++) {
            LocationResponseDto dto = places.get(i);

            Location loc = locationRepository.findByGooglePlaceId(dto.getGooglePlaceId())
                    .map(existing -> {
                        existing.setName(safeTrim(dto.getName(), 255));
                        existing.setCategory(safeTrim(dto.getCategory(), 100));
                        existing.setAddress(safeTrim(dto.getAddress(), 255));
                        existing.setLat(safeDouble(dto.getLat(), 0.0));
                        existing.setLng(safeDouble(dto.getLng(), 0.0));
                        existing.setRating(dto.getRating());
                        existing.setUserRatingsTotal(dto.getUserRatingsTotal());
                        existing.setPhotoUrl(dto.getPhotoUrl());
                        return existing;
                    })
                    .orElseGet(() -> Location.builder()
                            .googlePlaceId(dto.getGooglePlaceId())
                            .name(safeTrim(dto.getName() != null ? dto.getName() : "Unknown", 255))
                            .category(safeTrim(dto.getCategory(), 100))
                            .address(safeTrim(dto.getAddress(), 255))
                            .lat(safeDouble(dto.getLat(), 0.0))
                            .lng(safeDouble(dto.getLng(), 0.0))
                            .rating(dto.getRating())
                            .userRatingsTotal(dto.getUserRatingsTotal())
                            .photoUrl(dto.getPhotoUrl())
                            .build());

            try {
                Location saved = locationRepository.save(loc);

                placeResultItemRepository.save(
                        PlaceResultItem.builder()
                                .resultId(result.getResultId())
                                .placeId(saved.getPlaceId())
                                .isMain(i < mainCount)
                                .rank(rank++)
                                .build()
                );

            } catch (DataIntegrityViolationException e) {
                System.err.printf(
                        "[places upsert 실패] gpid=%s, name.len=%s, addr.len=%s, category.len=%s%n",
                        dto.getGooglePlaceId(),
                        dto.getName() != null ? dto.getName().length() : null,
                        dto.getAddress() != null ? dto.getAddress().length() : null,
                        dto.getCategory() != null ? dto.getCategory().length() : null
                );
                throw e;
            }
        }

        return result.getResultId();
    }

    /**
     * 사용자 검색 처리 및 추천 기능
     */
    @Transactional
    public SearchResponseDto searchLocation(SearchRequestDto request) {
        User user = userRepository.findByKakaoId(request.getKakaoId());
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        SearchHistory history = new SearchHistory(
                user,
                request.getQuery(),
                null // locationInfo는 나중에 LocationService 호출 후 채움
        );
        searchHistoryRepository.save(history);

        List<SearchHistory> recentHistory = searchHistoryRepository
                .findTop10ByUserOrderByCreatedAtDesc(user);

        List<String> recommendedPlaces = new ArrayList<>();
        for (SearchHistory h : recentHistory) {
            if (!h.getKeyword().equals(request.getQuery())) {
                recommendedPlaces.add(h.getKeyword());
            }
        }

        SearchResponseDto response = new SearchResponseDto();
        response.setKeyword(request.getQuery());
        response.setRecommendedPlaces(recommendedPlaces);
        response.setMessage("검색 완료");

        return response;
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDto> pickExtras(Long resultId, int limit) {
        var items = placeResultItemRepository
                .pickRandomExtrasByResultId(resultId, PageRequest.of(0, limit));

        if (items.isEmpty()) return List.of();

        // place_id 모아서 Location 일괄 조회
        List<Long> placeIds = items.stream()
                .map(PlaceResultItem::getPlaceId)
                .distinct()
                .toList();

        Map<Long, Location> locationMap = locationRepository.findAllById(placeIds)
                .stream()
                .collect(Collectors.toMap(Location::getPlaceId, l -> l));

        // 엔티티 -> DTO 매핑
        return items.stream()
                .map(it -> locationMap.get(it.getPlaceId()))
                .filter(Objects::nonNull)
                .map(this::toLocationDto)
                .toList();
    }

    // (선택) userId만 받는 버전이 필요하면 같이 추가
    @Transactional(readOnly = true)
    public List<LocationResponseDto> pickExtrasForUser(Long userId, int limit) {
        var latest = placeResultRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("최근 검색 기록이 없습니다."));
        Long resultId = latest.getResultId();  // 엔티티에 따라 getId()일 수도 있음
        return pickExtras(resultId, limit);
    }

    private LocationResponseDto toLocationDto(Location loc) {
        return LocationResponseDto.builder()
                .googlePlaceId(loc.getGooglePlaceId())
                .name(loc.getName())
                .category(loc.getCategory())
                .address(loc.getAddress())
                .lat(loc.getLat())
                .lng(loc.getLng())
                .rating(loc.getRating())
                .userRatingsTotal(loc.getUserRatingsTotal())
                .photoUrl(loc.getPhotoUrl())   // DTO: photoUrl, 엔티티: photoUrl
                .build();
    }
}
