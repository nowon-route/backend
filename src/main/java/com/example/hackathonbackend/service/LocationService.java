// src/main/java/com/example/hackathonbackend/service/LocationService.java
package com.example.hackathonbackend.service;

import com.example.hackathonbackend.dto.LocationRequestDto;
import com.example.hackathonbackend.dto.LocationResponseDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final WebClient webClient;

    @Value("${google.places.api-key}")
    private String apiKey;

    @Value("${google.places.photo-maxwidth:800}")
    private int photoMaxWidth;

    public LocationService(@Value("${google.places.base-url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    /** Nearby Search → limit만큼 수집 */
    public List<LocationResponseDto> getNearbyPlaces(LocationRequestDto req) {
        List<LocationResponseDto> out = new ArrayList<>();
        String nextToken = null;

        while (out.size() < req.getLimit()) {
            GoogleNearbyPage page = callNearby(req, nextToken);
            if (page == null || page.results == null || page.results.isEmpty()) break;

            for (GoogleNearbyResult r : page.results) {
                if (out.size() >= req.getLimit()) break;
                out.add(mapToResponse(r));
            }

            nextToken = page.nextPageToken;
            if (nextToken == null) break;

            // next_page_token 유효화 대기 (구글 제약)
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }
        return out;
    }

    private GoogleNearbyPage callNearby(LocationRequestDto req, String nextToken) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("key", apiKey);

        if (nextToken == null) {
            q.add("location", req.getLat() + "," + req.getLng());
            q.add("radius", String.valueOf(req.getRadius()));
            if (req.getKeyword() != null && !req.getKeyword().isBlank()) q.add("keyword", req.getKeyword());
            if (req.getType() != null && !req.getType().isBlank()) q.add("type", req.getType());
        } else {
            q.add("pagetoken", nextToken);
        }

        return webClient.get()
                .uri(u -> u.path("/maps/api/place/nearbysearch/json")
                        .queryParams(q)
                        .build())
                .retrieve()
                .bodyToMono(GoogleNearbyPage.class)
                .onErrorResume(ex -> Mono.empty())
                .block();
    }

    private LocationResponseDto mapToResponse(GoogleNearbyResult r) {
        String category = (r.types != null && !r.types.isEmpty()) ? r.types.get(0) : null;
        String photoUrl = null;
        if (r.photos != null && !r.photos.isEmpty() && r.photos.get(0).photoReference != null) {
            photoUrl = String.format(
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photo_reference=%s&key=%s",
                    photoMaxWidth, r.photos.get(0).photoReference, apiKey
            );
        }

        double lat = 0.0, lng = 0.0;
        if (r.geometry != null && r.geometry.location != null) {
            lat = r.geometry.location.lat;
            lng = r.geometry.location.lng;
        }

        return LocationResponseDto.builder()
                .googlePlaceId(r.placeId)
                .name(r.name)
                .category(category)
                .address(r.vicinity)
                .lat(lat)
                .lng(lng)
                .rating(r.rating)
                .userRatingsTotal(r.userRatingsTotal)
                .photoUrl(photoUrl)
                .build();
    }


    // ======= Google Nearby Search 응답 DTO(내부 전용) =======
    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleNearbyPage {
        private List<GoogleNearbyResult> results;
        @JsonProperty("next_page_token")
        private String nextPageToken;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleNearbyResult {
        @JsonProperty("place_id") String placeId;
        String name;
        String vicinity;
        Double rating;
        @JsonProperty("user_ratings_total") Integer userRatingsTotal;
        Geometry geometry;
        List<Photo> photos;
        List<String> types;

        @Data @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Geometry {
            Location location;
        }

        @Data @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Location {
            double lat;
            double lng;
        }

        @Data @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Photo {
            @JsonProperty("photo_reference") String photoReference;
        }
    }
}