package com.example.hackathonbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ItineraryWithPlacesRequest {

    @NotBlank
    private String date;

    @NotBlank
    private String companion;

    @NotBlank
    private String concept;

    private String area;                 // 선택

    // 프런트가 지도에서 선택한 장소들 그대로
    private List<PlaceInput> places;

    @Data
    public static class PlaceInput {
        private String googlePlaceId;
        private String name;
        private String category;
        private String address;
        private Double lat;
        private Double lng;
        private Double rating;
        private Integer userRatingsTotal;
        private String photoUrl;
    }
}
