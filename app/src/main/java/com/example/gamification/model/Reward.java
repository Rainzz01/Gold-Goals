package com.example.gamification.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rewards")
public class Reward {

    @PrimaryKey(autoGenerate = true)
    public int rewardId;

    public String name;
    public String description;
    public int requiredCoins;
    public int levelRequirement;
    public int imageId;

    public Reward() {}

    public Reward(String name, String description, int requiredCoins, int levelRequirement, int imageId) {
        this.name = name;
        this.description = description;
        this.requiredCoins = requiredCoins;
        this.levelRequirement = levelRequirement;
        this.imageId = imageId;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRequiredCoins() { return requiredCoins; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getImageId() { return imageId; }
}
