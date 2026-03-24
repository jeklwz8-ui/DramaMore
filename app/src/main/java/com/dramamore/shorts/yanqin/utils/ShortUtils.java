package com.dramamore.shorts.yanqin.utils;

import android.content.Context;

import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.dao.FollowDao;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.FollowDatabase;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.google.gson.Gson;

public class ShortUtils {
    private static final Gson gson = new Gson();
    private static final String TAG = "ShortUtils";

    public static String convertToK(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        // 除以1000.0 转成浮点数，保留一位小数
        return String.format("%.1fK", count / 1000.0f);
    }

    public static String shortPlayToJson(ShortPlay shortPlay) {
        return gson.toJson(shortPlay);
    }

    public static ShortPlay jsonToShortPlay(String json) {
        return gson.fromJson(json, ShortPlay.class);
    }

    public static void followInsertOrDelete(Context context, boolean isCollect, ShortPlay shortPlay, int playIndex) {
        Logs.i(TAG, "insertOrDelete-isCollect=" + isCollect + ",sid=" + shortPlay.id);
        FollowDatabase db = FollowDatabase.getDatabase(context);
        FollowDao followDao = db.followDao();
        FollowDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                if (isCollect) {
                    followDao.insert(new FollowDaoEntity(shortPlay.id, ShortUtils.shortPlayToJson(shortPlay), playIndex));
                } else {
                    followDao.deleteByShortId(shortPlay.id);
                }
            }
        });
    }

    public static void historyInsert(Context context, ShortPlay shortPlay, int playIndex) {
        Logs.i(TAG, "historyInsertOrDelete-sid=" + shortPlay.id + ",playIndex=" + playIndex);
        HistoryDatabase db = HistoryDatabase.getDatabase(context);
        HistoryDao historyDao = db.historyDao();
        HistoryDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                historyDao.insert(new HistoryDaoEntity(shortPlay.id, ShortUtils.shortPlayToJson(shortPlay), playIndex));
            }
        });
    }

}
