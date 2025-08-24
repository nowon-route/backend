package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.PlaceResultItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceResultItemRepository extends JpaRepository<PlaceResultItem, Long> {
}