package com.example.gamification.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "badges")
public class Badge {
    @PrimaryKey(autoGenerate = true)
    private int badgeId;
    private String title;
    private String description;
    private int imageId;
    private int xpReward;
    private boolean isUnlocked;

    // Room will use this constructor
    public Badge() {}

    // Mark this as ignored by Room
    @Ignore
    public Badge(String title, String description, int imageId, int xpReward, boolean isUnlocked) {
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.xpReward = xpReward;
        this.isUnlocked = isUnlocked;
    }

    // Getters and setters remain the same
    public int getBadgeId() { return badgeId; }
    public void setBadgeId(int badgeId) { this.badgeId = badgeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}