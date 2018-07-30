package com.ywl01.baidu.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class CameraBean implements Parcelable{
    public long id;
    public String monitorID;
    public String name;
    public String type;
    public double y;
    public double x;
    public String owner;
    public float angle;
    public long userID;
    public float displayLevel;
    public String telephone;
    public int isRunning;
    public String insertUser;


    protected CameraBean(Parcel in) {
        id = in.readLong();
        monitorID = in.readString();
        name = in.readString();
        type = in.readString();
        y = in.readDouble();
        x = in.readDouble();
        owner = in.readString();
        angle = in.readFloat();
        userID = in.readLong();
        displayLevel = in.readFloat();
        telephone = in.readString();
        isRunning = in.readInt();
        insertUser = in.readString();
    }

    public static final Creator<CameraBean> CREATOR = new Creator<CameraBean>() {
        @Override
        public CameraBean createFromParcel(Parcel in) {
            return new CameraBean(in);
        }

        @Override
        public CameraBean[] newArray(int size) {
            return new CameraBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(monitorID);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeDouble(y);
        dest.writeDouble(x);
        dest.writeString(owner);
        dest.writeFloat(angle);
        dest.writeLong(userID);
        dest.writeFloat(displayLevel);
        dest.writeString(telephone);
        dest.writeInt(isRunning);
        dest.writeString(insertUser);
    }
}
