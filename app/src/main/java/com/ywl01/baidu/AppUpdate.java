package com.ywl01.baidu;

import android.app.Activity;
import android.content.DialogInterface;

import com.ywl01.baidu.activitys.BaseActivity;
import com.ywl01.baidu.beans.UpdateInfoBean;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.UpdateInfoObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;
import com.ywl01.baidu.utils.DownloadUtils;

import rx.Observer;

/**
 * Created by ywl01 on 2018/3/8.
 */

public class AppUpdate implements BaseObserver.OnNextListener{

    private UpdateInfoObserver updateInfoObserver;
    private UpdateInfoBean updateInfoBean;
    private Activity activity;

    public AppUpdate() {
        activity = BaseActivity.currentActivity;
        updateInfoObserver = new UpdateInfoObserver();
        updateInfoObserver.setOnNextListener(this);
        HttpMethods.getInstance().getUpdateInfo(updateInfoObserver);
    }

    @Override
    public void onNext(Object data, Observer observer) {
        int versionCode = AppUtils.getVersionCode(activity);
        updateInfoBean = (UpdateInfoBean) data;
        if (versionCode != updateInfoBean.versionCode) {
            DialogUtils.showAlert(
                    activity,
                    updateInfoBean.title,
                    updateInfoBean.appDesc,
                    "确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DownloadUtils.getInstance(activity).startDownload(updateInfoBean.downloadUrl, updateInfoBean.title, updateInfoBean.appDesc, AppUtils.getAppName(activity));
                        }
                    },
                    "取消",
                    null);
        }
    }
}
