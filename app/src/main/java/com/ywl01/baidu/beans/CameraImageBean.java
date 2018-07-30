package com.ywl01.baidu.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ywl01 on 2017/3/13.
 */

public class CameraImageBean implements Parcelable{
    public long id;
    public long monitorID;
    public String imageUrl;
    public String thumbUrl;
    public String insertUser;
    public String insertTime;

    protected CameraImageBean(Parcel in) {
        id = in.readLong();
        monitorID = in.readLong();
        imageUrl = in.readString();
        thumbUrl = in.readString();
        insertUser = in.readString();
        insertTime = in.readString();
    }

    public static final Creator<CameraImageBean> CREATOR = new Creator<CameraImageBean>() {
        @Override
        public CameraImageBean createFromParcel(Parcel in) {
            return new CameraImageBean(in);
        }

        @Override
        public CameraImageBean[] newArray(int size) {
            return new CameraImageBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(monitorID);
        dest.writeString(imageUrl);
        dest.writeString(thumbUrl);
        dest.writeString(insertUser);
        dest.writeString(insertTime);
    }
}
