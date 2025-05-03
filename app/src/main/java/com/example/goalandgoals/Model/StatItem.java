package com.example.goalandgoals.Model;

public class StatItem {
    private final String title;
    private final String value;

    public StatItem(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }
}