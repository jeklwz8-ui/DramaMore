package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

public class AppUtils {

    private static String TAG = "AppUtils";

    public static void launchAppByPackageName(Context context, String packageName) {
        try {
            // 获取应用启动 Intent
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            } else {
                Logs.i(TAG, "应用未安装或未找到入口!");
            }
        } catch (Exception e) {
            Logs.i(TAG, "应用跳转异常：" + e.getMessage());
        }
    }

    /**
     * 唤起外部浏览器打开 URL
     *
     * @param url 要打开的链接
     */
    public static void openUrlInExternalBrowser(Context context, String url) {
        try {
            if(url.startsWith("file://")){
                url=url.replace("file://","");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // 标记为外部浏览器打开，避免唤起其他应用
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有可用的浏览器应用
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }else{
                openLocalHtmlInBrowser(context,url);
                Logs.i(TAG, "应用跳转失败-url=" + url);
            }
        } catch (Exception e) {
            Logs.i(TAG, "应用跳转异常：" + e.getMessage());
        }
    }

    public static void openLocalHtmlInBrowser(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Logs.i(TAG, "文件不存在: " + filePath);
                return;
            }

            // 1. 生成安全的 content:// 格式 URI
            Uri contentUri = FileProvider.getUriForFile(context,context.getPackageName() + ".fileprovider", file);

            // 2. 创建 Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 必须指定数据类型为 text/html
            intent.setDataAndType(contentUri, "text/html");

            // 3. 关键：授予外部应用临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 4. 启动跳转
            context.startActivity(intent);

        } catch (Exception e) {
            Logs.i(TAG, "跳转浏览器失败: " + e.getMessage());
        }
    }

}
