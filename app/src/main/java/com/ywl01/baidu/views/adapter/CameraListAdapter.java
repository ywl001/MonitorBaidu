package com.ywl01.baidu.views.adapter;

import android.widget.AbsListView;

import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.views.holders.BaseHolder;
import com.ywl01.baidu.views.holders.CameraItemHolder;

import java.util.List;

/**
 * Created by ywl01 on 2017/3/15.
 */

public class CameraListAdapter extends SuperBaseAdapter<CameraBean> {

    public CameraListAdapter(AbsListView listView, List datas) {
        super(listView, datas);
    }

    @Override
    protected BaseHolder getItemHolder(int position) {
        return new CameraItemHolder();
    }
}
