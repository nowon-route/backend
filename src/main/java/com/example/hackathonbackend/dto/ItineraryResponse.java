// src/main/java/com/example/hackathonbackend/dto/ItineraryResponse.java
package com.example.hackathonbackend.dto;

import lombok.Getter; import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ItineraryResponse {
    private String area;
    private String date;
    private String companion;
    private String concept;
    private List<ItineraryStop> itinerary;
    private List<String> warnings;
    private List<String> reasons;
}
