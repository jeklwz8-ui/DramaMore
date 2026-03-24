package com.dramamore.shorts.yanqin.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryDaoEntity entity);

    // 根据 short_id 查询单条数据
    @Query("SELECT * FROM history_tbn WHERE short_id = :shortId LIMIT 1")
    HistoryDaoEntity getEntityByShortId(long shortId);

    // 根据 short_id 删除
    @Query("DELETE FROM history_tbn WHERE short_id = :shortId")
    void deleteByShortId(long shortId);

    //getPagedFollows(20, (page - 1) * 20)
    @Query("SELECT * FROM history_tbn ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<HistoryDaoEntity> getPagedHistories(int limit, int offset);
}
