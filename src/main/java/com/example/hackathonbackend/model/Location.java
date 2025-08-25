package com.example.hackathonbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "places",
        uniqueConstraints = @UniqueConstraint(columnNames = "google_place_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "google_place_id", nullable = false, unique = true, length = 255)
    private String googlePlaceId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String category;

    // <= 여기만 변경: TEXT 로 여유 있게
    @Column(columnDefinition = "TEXT")
    private String address;

    private Double lat;
    private Double lng;
    private Double rating;

    @Column(name = "user_ratings_total")
    private Integer userRatingsTotal;

    @Lob
    @Column(name = "photo_url",columnDefinition = "TEXT")
    private String photoUrl;
}