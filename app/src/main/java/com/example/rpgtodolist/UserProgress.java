package com.example.rpgtodolist;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_progress")
public class UserProgress {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int xp;      // 经验值
    private int coins;   // 金币数

    // Constructor
    public UserProgress(int xp, int coins) {
        this.xp = xp;
        this.coins = coins;
    }

    // Getter 和 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
