package com.ywl01.baidu.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Created by ywl01 on 2018/2/25.
 */

public class DownloadUtils {

    private static DownloadUtils instance;

    private DownloadManager downloadManager;
    private Context context;

    private DownloadUtils(Context context) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.context = context.getApplicationContext();
    }

    public static DownloadUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadUtils(context);
        }
        return instance;
    }

    public void startDownload(String uri, String title, String desc, String appName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
        //下载时允许的网络类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //设置文件的保存的位置[三种方式]
        //第一种
        //file:///storage/emulated/0/Android/data/your-package/files/Download/update.apk
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, appName + ".apk");

        //第二种
        //file:///storage/emulated/0/Download/update.apk
        //req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk");

        //第三种 自定义文件路径
        //req.setDestinationUri()

        // 设置一些基本显示信息
        request.setTitle(title);
        request.setDescription(desc);
        //req.setMimeType("application/vnd.android.package-archive");

        long downloadID = downloadManager.enqueue(request);//异步
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(DownloadManager.EXTRA_DOWNLOAD_ID,downloadID).commit();
    }

    /**
     * 获取文件保存的路径
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return file path
     * @see DownloadUtils#getDownloadUri(long)
     */
    public String getDownloadPath(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                }
            } finally {
                c.close();
            }
        }
        return null;
    }

    /**
     * 获取保存文件的地址
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @see DownloadUtils#getDownloadPath(long)
     */
    public Uri getDownloadUri(long downloadId) {
        return downloadManager.getUriForDownloadedFile(downloadId);
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * 获取下载状态
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING
     * @see DownloadManager#STATUS_PAUSED
     * @see DownloadManager#STATUS_RUNNING
     * @see DownloadManager#STATUS_SUCCESSFUL
     * @see DownloadManager#STATUS_FAILED
     */
    public int getDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }
}
