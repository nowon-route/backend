// src/main/java/com/example/hackathonbackend/dto/LocationResponseDto.java
package com.example.hackathonbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationResponseDto {
    private String googlePlaceId;     // place_id
    private String name;              // name
    private String category;          // types[0] 등 대표 1개
    private String address;           // vicinity
    private double lat;               // geometry.location.lat
    private double lng;               // geometry.location.lng
    private Double rating;            // rating
    private Integer userRatingsTotal; // user_ratings_total
    private String photoUrl;          // photo_reference -> photo API URL
}