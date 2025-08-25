package com.example.hackathonbackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class Constraints {
        private List<String> mustInclude;
        private List<String> mustExclude;
        private OpeningHours openingHours; // ✅ 추가 (getOpeningHours() 생성됨)
}
