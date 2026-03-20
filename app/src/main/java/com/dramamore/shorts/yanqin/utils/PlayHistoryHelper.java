package com.dramamore.shorts.yanqin.utils;

import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;

public class PlayHistoryHelper {

    private static final ArrayList<PlayHistory> playHistoryList = new ArrayList<>();

    public static void savePlayHistory(PlayHistory newPlayHistory) {
        if (newPlayHistory == null || newPlayHistory.shortPlay == null) {
            return;
        }
        for (PlayHistory history : playHistoryList) {
            if (history.shortPlay.id == newPlayHistory.shortPlay.id) {
                playHistoryList.remove(history);
                break;
            }
        }
        playHistoryList.add(0, newPlayHistory);
    }

    public static ArrayList<PlayHistory> getPlayHistory() {
        return playHistoryList;
    }

    public static PlayHistory getLastWatchShortPlay() {
        return playHistoryList.isEmpty() ? null : playHistoryList.get(0);
    }

    public static class PlayHistory {
        public ShortPlay shortPlay;
        public int index;
        public int seconds;
    }
}
