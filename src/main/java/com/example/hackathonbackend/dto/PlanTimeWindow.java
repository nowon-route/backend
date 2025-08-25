// src/main/java/com/example/hackathonbackend/dto/PlanTimeWindow.java
package com.example.hackathonbackend.dto;

import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class PlanTimeWindow {
    private String start; // "HH:mm"
    private String end;   // "HH:mm"
}
