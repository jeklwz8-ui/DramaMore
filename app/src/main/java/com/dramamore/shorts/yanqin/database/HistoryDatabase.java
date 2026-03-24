package com.dramamore.shorts.yanqin.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dramamore.shorts.yanqin.dao.FollowDao;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HistoryDaoEntity.class}, version = 1, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {

    private static volatile HistoryDatabase INSTANCE;
    // 定义一个固定的单线程池，专门给数据库用
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 关联你的 Dao
    public abstract HistoryDao historyDao();

    // 获取单例的方法
    public static HistoryDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HistoryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    HistoryDatabase.class, "history_database") // 数据库文件名
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

