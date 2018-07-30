package com.ywl01.baidu.net.observers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ywl01.baidu.beans.CameraImageBean;

import java.util.List;

/**
 * Created by ywl01 on 2017/3/13.
 */

public class ImageObserver extends BaseObserver<String>{
    @Override
    protected List<CameraImageBean> transform(String data) {
        return new Gson().fromJson(data, new TypeToken<List<CameraImageBean>>() {}.getType());
    }
}
