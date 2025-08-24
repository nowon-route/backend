package com.example.hackathonbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_result_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Builder.Default
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    @Column(name = "rank")
    private Integer rank;
}