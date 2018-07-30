package com.ywl01.baidu.net.observers;

import com.ywl01.baidu.beans.UpdateInfoBean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ywl01 on 2018/2/23.
 */

public class UpdateInfoObserver extends BaseObserver<String> {
    @Override
    protected UpdateInfoBean transform(String data) {
        UpdateInfoBean updateInfoBean = new UpdateInfoBean();
        try {
            JSONObject jsonObject = new JSONObject(data);
            updateInfoBean.appDesc = jsonObject.getString("desc");
            updateInfoBean.downloadUrl = jsonObject.getString("downloadUrl");
            updateInfoBean.versionCode = jsonObject.getInt("versionCode");
            return updateInfoBean;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
