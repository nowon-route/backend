package com.example.hackathonbackend.util;

public final class CategoryMapper {
    private CategoryMapper() {}
    public static String map(String googleTypeOrHint) {
        if (googleTypeOrHint == null) return "other";
        String t = googleTypeOrHint.toLowerCase();
        if (t.contains("cafe")) return "cafe";
        if (t.contains("observatory") || t.contains("night_view") || t.contains("view")) return "night_view";
        if (t.contains("restaurant")) return "restaurant";
        if (t.contains("park")) return "park";
        return "other";
    }
}
