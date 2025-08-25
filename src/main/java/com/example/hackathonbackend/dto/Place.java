package com.example.hackathonbackend.dto;

public record Place(
        String googlePlaceId,
        String name,
        String category,
        String address,
        double lat,
        double lng,
        Double rating,
        Integer userRatingsTotal,
        String photoUrl
) {}
