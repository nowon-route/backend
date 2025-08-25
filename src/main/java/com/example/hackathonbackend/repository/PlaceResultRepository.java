package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.PlaceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaceResultRepository extends JpaRepository<PlaceResult, Long> {
    Optional<PlaceResult> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}