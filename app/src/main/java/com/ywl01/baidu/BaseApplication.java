package com.ywl01.baidu;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by ywl01 on 2016/12/31.
 */

public class BaseApplication extends Application {

    private static Context appContext;
    private static int mainThreadID;
    private static Handler mainThreadHandler;
    private static Looper mainThreadLooper;
    private static Thread mainThread;
    public BMapManager mapManager;
    public static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        //SDKInitializer.initialize(getApplicationContext());，因此我们建议该方法放在Application的初始化方法中
        SDKInitializer.initialize(getApplicationContext());

        appContext = this;
        mainThreadHandler = new Handler();
        mainThreadID = Process.myTid();
        mainThread = Thread.currentThread();
        mainThreadLooper = getMainLooper();

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder() //
                .showImageForEmptyUri(R.drawable.ic_launcher) //
                .showImageOnFail(R.drawable.ic_launcher) //
                .cacheInMemory(true) //
                .cacheOnDisk(true) //
                .build();//
        ImageLoaderConfiguration config = new ImageLoaderConfiguration//
                .Builder(getApplicationContext())//
                .defaultDisplayImageOptions(defaultOptions)//
                .discCacheSize(50 * 1024 * 1024)//
                .discCacheFileCount(100)// 缓存一百张图片
                .writeDebugLogs()//
                .build();//
        ImageLoader.getInstance().init(config);

        instance = this;
        initEngineManager(this);
    }

    public void initEngineManager(Context context) {
        if (mapManager == null) {
            mapManager = new BMapManager(context);
        }

        if (!mapManager.init(new MyGeneralListener())) {
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d("ljx", "initEngineManager");
    }

    public static BaseApplication getInstance() {
        return instance;
    }


    public static int getMainThreadID() {
        return mainThreadID;
    }

    public static Handler getMainHandler() {
        return mainThreadHandler;
    }

    public static Looper getMainThreadLooper() {
        return mainThreadLooper;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static Context getAppContext() {
        return appContext;
    }


    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(BaseApplication.getInstance().getApplicationContext(),
                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG).show();
            }
        }
    }


}
