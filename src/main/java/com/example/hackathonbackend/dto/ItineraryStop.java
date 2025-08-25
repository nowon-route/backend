package com.example.hackathonbackend.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryStop {
    private Integer order;        // ← Integer 로!
    private String googlePlaceId;
    private String name;
    private String category;

    private String address;
    private double lat;
    private double lng;
    private String photoUrl;          // optional
    private double rating;            // 0.0 가능
    private int userRatingsTotal;     // 0 가능

    private String arriveAt;          // "HH:mm"
    private int stayMinutes;
}
