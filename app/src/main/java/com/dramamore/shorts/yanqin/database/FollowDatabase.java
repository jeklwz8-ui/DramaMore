package com.dramamore.shorts.yanqin.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dramamore.shorts.yanqin.dao.FollowDao;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {FollowDaoEntity.class}, version = 1, exportSchema = false)
public abstract class FollowDatabase extends RoomDatabase {

    private static volatile FollowDatabase INSTANCE;
    // 定义一个固定的单线程池，专门给数据库用
    public static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    // 关联你的 Dao
    public abstract FollowDao followDao();

    // 获取单例的方法
    public static FollowDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FollowDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FollowDatabase.class, "follow_database") // 数据库文件名
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

