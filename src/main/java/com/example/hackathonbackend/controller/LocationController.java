// src/main/java/com/example/hackathonbackend/controller/LocationController.java
package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.LocationRequestDto;
import com.example.hackathonbackend.dto.LocationResponseDto;
import com.example.hackathonbackend.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/places/nearby")
    public ResponseEntity<List<LocationResponseDto>> nearby(@Valid LocationRequestDto req) {
        return ResponseEntity.ok(locationService.getNearbyPlaces(req));
    }
}
