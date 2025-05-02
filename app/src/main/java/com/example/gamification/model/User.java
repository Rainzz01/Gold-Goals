package com.example.gamification.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String name;

    public int totalCoins;

    public int exp;

    public int level;

    public User(String name, int totalCoins, int exp, int level) {
        this.name = name;
        this.totalCoins = totalCoins;
        this.exp = exp;
        this.level = level;
    }
}
