package com.example.hackathonbackend.dto;

import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class Place {
    private String googlePlaceId;
    private String name;
    private String category;
    private String address;
    private double lat;
    private double lng;
    private Double rating;
    private Integer userRatingsTotal;
    private String photoUrl; // Google photo URL (key 포함 시 프록시 권장)
}
