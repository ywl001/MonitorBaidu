package com.ywl01.baidu.events;

import com.ywl01.baidu.beans.CameraBean;

/**
 * Created by ywl01 on 2017/3/13.
 */

public class CameraInfoEvent extends Event{
    public static final  int SHOW_CAMERA_INFO_VIEW = 1;
    public static final  int HIDE_CAMERA_INFO_VIEW = 0;

    public CameraBean cameraBean;
    public int type;

    public CameraInfoEvent(int type) {
        this.type = type;
    }
}
