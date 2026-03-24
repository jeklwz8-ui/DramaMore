package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SPUtils {
    private static volatile SPUtils instance;
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    // 私有构造方法，防止外部实例化
    private SPUtils(Context context, String fileName) {
        sp = context.getApplicationContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    // 单例模式：
    public static SPUtils getInstance(Context context) {
        return getInstance(context, context.getPackageName()+"config");
    }

    // 重载：支持自定义文件名
    private static SPUtils getInstance(Context context, String fileName) {
        if (instance == null) {
            synchronized (SPUtils.class) {
                if (instance == null) {
                    instance = new SPUtils(context, fileName);
                }
            }
        }
        return instance;
    }

    // ======== 存储数据 ========
    public void putString(String key, String value) {
        editor.putString(key, value).apply(); // 异步提交（推荐）
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value).apply();
    }

    // ======== 获取数据 ========
    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    // ======== 其他操作 ========
    // 删除指定key
    public void remove(String key) {
        editor.remove(key).apply();
    }

    // 清空所有数据
    public void clear() {
        editor.clear().apply();
    }

    // 判断key是否存在
    public boolean contains(String key) {
        return sp.contains(key);
    }

    // 获取所有键值对
    public Map<String, ?> getAll() {
        return sp.getAll();
    }
}

