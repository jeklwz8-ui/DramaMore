package com.dramamore.shorts.yanqin.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static String saveStringToFile(Context context, String fileName, String content) {
        // 1. 获取内部存储路径 (data/data/包名/files)
        File file = new File(context.getFilesDir(), fileName);

        // 2. 使用 Try-with-resources 自动关闭流
        // 使用 FileOutputStream 配合 UTF-8 编码比 FileWriter 更稳妥
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            // 返回文件的绝对路径
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 或者抛出异常
        }
    }


}
