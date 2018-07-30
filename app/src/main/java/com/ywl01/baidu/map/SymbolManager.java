package com.ywl01.baidu.map;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.ywl01.baidu.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ywl01 on 2017/3/15.
 */

public class SymbolManager {

    public static BitmapDescriptor normal = BitmapDescriptorFactory.fromResource(R.drawable.normal);

    public static BitmapDescriptor qiangji = BitmapDescriptorFactory.fromResource(R.drawable.qiangji);
    public static BitmapDescriptor qiuji = BitmapDescriptorFactory.fromResource(R.drawable.qiuji);
    public static BitmapDescriptor kakou = BitmapDescriptorFactory.fromResource(R.drawable.kakou);
    public static BitmapDescriptor dianjing = BitmapDescriptorFactory.fromResource(R.drawable.dianjing);
    public static BitmapDescriptor shehui = BitmapDescriptorFactory.fromResource(R.drawable.shehui);
    public static BitmapDescriptor gaokong = BitmapDescriptorFactory.fromResource(R.drawable.gaokong);
    public static BitmapDescriptor quanjing = BitmapDescriptorFactory.fromResource(R.drawable.quanjing);

    public static BitmapDescriptor qiangji_press = BitmapDescriptorFactory.fromResource(R.drawable.qiangji_press);
    public static BitmapDescriptor dianjing_press = BitmapDescriptorFactory.fromResource(R.drawable.qiangji_press);
    public static BitmapDescriptor qiuji_press = BitmapDescriptorFactory.fromResource(R.drawable.qiuji_press);
    public static BitmapDescriptor kakou_press = BitmapDescriptorFactory.fromResource(R.drawable.kakou_press);
    public static BitmapDescriptor shehui_press = BitmapDescriptorFactory.fromResource(R.drawable.shehui_press);
    public static BitmapDescriptor gaokong_press = BitmapDescriptorFactory.fromResource(R.drawable.gaokong_press);
    public static BitmapDescriptor quanjing_press = BitmapDescriptorFactory.fromResource(R.drawable.quanjing_press);

    public static BitmapDescriptor qiangji_error = BitmapDescriptorFactory.fromResource(R.drawable.qiangji_error);
    public static BitmapDescriptor dianjing_error = BitmapDescriptorFactory.fromResource(R.drawable.qiangji_press);
    public static BitmapDescriptor qiuji_error = BitmapDescriptorFactory.fromResource(R.drawable.qiuji_error);
    public static BitmapDescriptor kakou_error = BitmapDescriptorFactory.fromResource(R.drawable.kakou_error);
    public static BitmapDescriptor shehui_error = BitmapDescriptorFactory.fromResource(R.drawable.shehui_error);
    public static BitmapDescriptor gaokong_error = BitmapDescriptorFactory.fromResource(R.drawable.gaokong_error);
    public static BitmapDescriptor quanjing_error = BitmapDescriptorFactory.fromResource(R.drawable.quanjing_error);

    private static HashMap<String, BitmapDescriptor> getSymbolMap() {
        HashMap<String, BitmapDescriptor> map = new HashMap<>();
        map.put("枪机", qiangji);
        map.put("球机", qiuji);
        map.put("卡口", kakou);
        map.put("电警", dianjing);
        map.put("社会监控", shehui);
        map.put("高空瞭望", gaokong);
        map.put("全景", quanjing);

        map.put("枪机_press", qiangji_press);
        map.put("球机_press", qiuji_press);
        map.put("卡口_press", kakou_press);
        map.put("电警_press", dianjing_press);
        map.put("社会监控_press", shehui_press);
        map.put("高空瞭望_press", gaokong_press);
        map.put("全景_press", quanjing_press);

        map.put("枪机_error", qiangji_error);
        map.put("球机_error", qiuji_error);
        map.put("卡口_error", kakou_error);
        map.put("电警_error", dianjing_error);
        map.put("社会监控_error", shehui_error);
        map.put("高空瞭望_error", gaokong_error);
        map.put("全景_error", quanjing_error);
        return map;
    }

    public static BitmapDescriptor getIconByName(String name) {
        HashMap<String, BitmapDescriptor> map = getSymbolMap();
        BitmapDescriptor icon = map.get(name);
        if (icon == null) {
            return normal;
        }
        return icon;
    }

    public static String getNameByIcon(BitmapDescriptor icon) {
        HashMap<String, BitmapDescriptor> map = getSymbolMap();
        for (Map.Entry entry : map.entrySet()) {
            if (icon.equals(entry.getValue()))
                return (String) entry.getKey();
        }
        return null;
    }

}
