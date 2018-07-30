package com.ywl01.baidu.views.holders;

import android.view.View;
import android.widget.TextView;

import com.ywl01.baidu.R;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ywl01 on 2017/3/15.
 */

public class CameraItemHolder extends BaseHolder<CameraBean> {

    @Bind(R.id.tv_name)
    TextView tvName;

    @Bind(R.id.tv_type)
    TextView tvType;

    @Bind(R.id.tv_owner)
    TextView tvOwner;

    @Bind(R.id.tv_telephone)
    TextView tvTelephone;


    @Override
    protected View initView() {
        View view = View.inflate(AppUtils.getContext(), R.layout.item_list, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void refreshUI(CameraBean data) {
        if(StringUtils.isEmpty(data.monitorID) && StringUtils.isEmpty(data.name))
            tvName.setVisibility(View.GONE);
        else if(StringUtils.isEmpty(data.monitorID))
            tvName.setText(data.name);
        else if(StringUtils.isEmpty(data.name))
            tvName.setText(data.monitorID);
        else
            tvName.setText(data.monitorID + "—" + data.name);

        tvType.setText(data.type);
        tvOwner.setText(data.owner);
        tvTelephone.setText(data.telephone);
    }
}
