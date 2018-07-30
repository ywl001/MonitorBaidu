package com.ywl01.baidu.net.observers;

import android.os.Bundle;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.map.MapListener;
import com.ywl01.baidu.map.SymbolManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控标注的数据源
 */

public class MarkerObserver extends BaseObserver<String> {
    @Override
    protected List<OverlayOptions> transform(String json) {
        List<CameraBean> markers = new Gson().fromJson(json, new TypeToken<List<CameraBean>>() {
        }.getType());
        System.out.println("cameraBeans size:" + markers.size());

        List<OverlayOptions> optionsList = new ArrayList<>();

        for (int i = 0; i < markers.size(); i++) {
            CameraBean cameraBean = markers.get(i);
            MarkerOptions options = beanToOptions(cameraBean);
            optionsList.add(options);
        }
        return optionsList;
    }

    private BitmapDescriptor getBitmapDesc(String type, int isRunning) {
        BitmapDescriptor bitmapDesc = null;
        if (isRunning == 1) {
            bitmapDesc = SymbolManager.getIconByName(type);
        } else {
            bitmapDesc = SymbolManager.getIconByName(type + "_error");
        }
        return bitmapDesc;
    }

    private MarkerOptions beanToOptions(CameraBean cameraBean) {
        MarkerOptions options = new MarkerOptions();
        LatLng latLng = new LatLng(cameraBean.x, cameraBean.y);
        options.position(latLng);

        BitmapDescriptor icon = getBitmapDesc(cameraBean.type, cameraBean.isRunning);

        if (cameraBean.id == MapListener.highLightID) {
            icon = SymbolManager.getIconByName(cameraBean.type + "_press");
            options.icon(icon);
        } else{
            options.icon(icon);
        }

        options.anchor(0.5f, 0.5f);
        options.rotate(360 - cameraBean.angle);

        Bundle extraInfo = new Bundle();
        extraInfo.putParcelable("data", cameraBean);
        options.extraInfo(extraInfo);

        options.animateType(MarkerOptions.MarkerAnimateType.grow);
        return options;
    }
}
