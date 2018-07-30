package com.ywl01.baidu.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.ywl01.baidu.R;
import com.ywl01.baidu.events.UploadImageStartEvent;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ywl01 on 2017/2/14.
 */

public class UploadImageMenuDialog extends Dialog {
    private Context context;
    public Map<String,String> data;

    public UploadImageMenuDialog(Context context) {
        super(context);
        this.context = context;
        setTitle("请选择获取照片的途径：");
    }

    public UploadImageMenuDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        setTitle("请选择获取照片的途径：");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_upload_image, null);
        ButterKnife.bind(this,view);
        setContentView(view);
        setTitle("请选择获取照片的途径：");
    }

    @OnClick(R.id.btn_photos)
    public void onSelect() {
        UploadImageStartEvent event = new UploadImageStartEvent(UploadImageStartEvent.FROM_PHOTOS);
        event.IMAGE_DIR = (String) data.get("imageDir");
        event.id = data.get("id");
        event.dispatch();
        dismiss();
    }

    @OnClick(R.id.btn_camera)
    public void onCamera() {
        UploadImageStartEvent event = new UploadImageStartEvent(UploadImageStartEvent.FROM_CAMERS);
        event.IMAGE_DIR = (String) data.get("imageDir");
        event.id = data.get("id");
        event.dispatch();
        dismiss();
    }



}
