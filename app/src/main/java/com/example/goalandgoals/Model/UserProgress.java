package com.example.goalandgoals.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
@Entity(tableName = "user_progress")
public class UserProgress {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int xp;
    private int coins;
    private String Name;

    public UserProgress() {
    }

    public UserProgress(int id, int xp, int coins) {
        this.id = id;
        this.xp = xp;
        this.coins = coins;
    }

    @PropertyName("id")
    public int getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(int id) {
        this.id = id;
    }

    @PropertyName("xp")
    public int getXp() {
        return xp;
    }

    @PropertyName("xp")
    public void setXp(int xp) {
        this.xp = xp;
    }

    @PropertyName("coins")
    public int getCoins() {
        return coins;
    }

    @PropertyName("coins")
    public void setCoins(int coins) {
        this.coins = coins;
    }

    @PropertyName("name")
    public String getName() {
        return Name;
    }

    @PropertyName("name")
    public void setName(String name) {
        Name = name;
    }
}