package com.example.hackathonbackend.dto;

import lombok.Getter; import lombok.Setter;
import java.util.List;

@Getter @Setter
public class Constraints {
    private List<String> mustInclude;
    private List<String> mustExclude;
    private PlanTimeWindow openingHours; // ← 클래스명만 PlanTimeWindow로 변경
    private String budget;               // 예: "중간"
}
