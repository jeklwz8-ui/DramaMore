package com.dramamore.shorts.yanqin.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "follow_tbn",
        indices = {@Index(value = {"short_id"}, unique = true)} // 强制 short_id 唯一
)
public class FollowDaoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "short_id")
    public long short_id;

    @ColumnInfo(name = "short_json")
    public String short_json;

    @ColumnInfo(name = "play_index")
    public int play_index;

    public FollowDaoEntity(long short_id, String short_json, int play_index) {
        this.short_id = short_id;
        this.short_json = short_json;
        this.play_index = play_index;
    }

}

