package com.example.hackathonbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // http://localhost:8080/
    @GetMapping("/")
    public String home() {
        // src/main/resources/templates/index.html 렌더링
        return "index";
    }

    // http://localhost:8080/itinerary  또는  http://localhost:8080/itinerary.html
    @GetMapping({"/itinerary", "/itinerary.html"})
    public String itinerary() {
        // src/main/resources/templates/itinerary.html 렌더링
        return "itinerary";
    }
}
