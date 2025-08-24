package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByGooglePlaceId(String googlePlaceId);
}