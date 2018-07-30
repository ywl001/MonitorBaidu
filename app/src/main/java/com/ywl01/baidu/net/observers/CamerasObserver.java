package com.ywl01.baidu.net.observers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ywl01.baidu.beans.CameraBean;

import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 搜索监控的数据源
 */

public class CamerasObserver extends BaseObserver<String> {
    @Override
    protected List<CameraBean> transform(String json) {
        List<CameraBean> cameras = new Gson().fromJson(json, new TypeToken<List<CameraBean>>() {
        }.getType());
        return cameras;
    }
}
