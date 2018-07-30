package com.ywl01.baidu.events;

/**
 * Created by ywl01 on 2017/2/1.
 */

public class UploadImageStartEvent extends Event {
    public static final int FROM_PHOTOS = 1;
    public static final int FROM_CAMERS = 2;

    public int type;

    public String IMAGE_DIR;//服务器图片文件夹相对目录
    public String id;//操作对象的id

    public UploadImageStartEvent(int type) {
        this.type = type;
    }

}
