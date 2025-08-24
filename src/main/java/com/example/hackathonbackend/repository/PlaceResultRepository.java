package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.PlaceResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceResultRepository extends JpaRepository<PlaceResult, Long> {
}