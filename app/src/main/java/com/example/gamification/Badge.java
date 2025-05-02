package com.example.gamification;

public class Badge {
    private String name;
    private String description;
    private int iconResId;
    private int xpReward;
    private boolean unlocked;

    public Badge(String name, String description, int iconResId, int xpReward, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.xpReward = xpReward;
        this.unlocked = unlocked;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public int getXpReward() { return xpReward; }
    public boolean isUnlocked() { return unlocked; }
}