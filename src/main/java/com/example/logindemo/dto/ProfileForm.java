package com.example.logindemo.dto;

import lombok.Data;

@Data
public class ProfileForm {
    private String displayName;
    private String intro;
    private String profileImageUrl;
    private String thumbnailImageUrl;
}
