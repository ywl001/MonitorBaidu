package com.ywl01.baidu.views;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ywl01.baidu.R;
import com.ywl01.baidu.User;
import com.ywl01.baidu.activitys.BaseActivity;
import com.ywl01.baidu.activitys.EditMonitorActivity;
import com.ywl01.baidu.activitys.ImageDetailActivity;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.beans.CameraImageBean;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.consts.TableName;
import com.ywl01.baidu.consts.Urls;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.DelObserver;
import com.ywl01.baidu.net.observers.ImageObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;
import com.ywl01.baidu.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

import static com.ywl01.baidu.R.id.btn_del;
import static com.ywl01.baidu.R.id.btn_edit;
import static com.ywl01.baidu.R.id.btn_move;

/**
 * Created by ywl01 on 2017/3/12.
 * 监控信息显示的view
 */

public class CameraInfoView extends RelativeLayout implements BaseObserver.OnNextListener, View.OnClickListener, ImageGroup.DragListener {
    private Context context;
    private CameraBean cameraBean;
    private ImageObserver imageObserver;
    private DelObserver delImageObserver;

    private String delImageUrl;
    private String delThumbUrl;

    private ArrayList<CameraImageBean> monitorImages;

    @Bind(R.id.tv_name)
    TextView tvName;

    @Bind(R.id.tv_type)
    TextView tvType;

    @Bind(R.id.tv_owner)
    TextView tvOwner;

    @Bind(R.id.tv_telephone)
    TextView tvTelephone;

    @Bind(R.id.scroller_view)
    HorizontalScrollView scrollView;

    @Bind(R.id.image_group)
    ImageGroup imageGroup;

    @Bind(R.id.ll_menu)
    LinearLayout markerMenu;

    private DelObserver delFileObserver;

    public CameraInfoView(Context context) {
        super(context);
        this.context = context;
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        View view = View.inflate(context, R.layout.monitor_info, this);
        ButterKnife.bind(this, view);
        imageGroup.setDragListener(this);
    }

    public void setData(CameraBean data) {
        cameraBean = data;
        refreshUI(cameraBean);
    }

    private void refreshUI(CameraBean cameraBean) {
        if (StringUtils.isEmpty(cameraBean.monitorID) && StringUtils.isEmpty(cameraBean.name))
            tvName.setVisibility(View.GONE);
        else
            tvName.setVisibility(VISIBLE);

        if (StringUtils.isEmpty(cameraBean.monitorID))
            tvName.setText(cameraBean.name);
        else if (StringUtils.isEmpty(cameraBean.name))
            tvName.setText(cameraBean.monitorID);
        else
            tvName.setText(cameraBean.monitorID + "—" + cameraBean.name);

        tvType.setText(cameraBean.type);
        tvOwner.setText(cameraBean.owner);
        tvTelephone.setText(cameraBean.telephone);

        System.out.println("user id" + User.id);

        if (cameraBean.userID == User.id || "admin".equals(User.userType)) {
            markerMenu.setVisibility(VISIBLE);
        } else {
            markerMenu.setVisibility(GONE);
        }
        getMonitorImages();
    }

    //获取图像
    private void getMonitorImages() {
        imageObserver = new ImageObserver();
        String sql = SqlFactory.selectMonitorImage(cameraBean.id);
        HttpMethods.getInstance().getSqlResult(imageObserver, SqlAction.SELECT, sql);
        imageObserver.setOnNextListener(this);
    }

    private void setMonitorImages() {
        if (monitorImages != null && monitorImages.size() > 0) {
            scrollView.setVisibility(View.VISIBLE);
            imageGroup.removeAllViews();
            for (int i = 0; i < monitorImages.size(); i++) {
                String thumbUrl = monitorImages.get(i).thumbUrl;
                ImageView imageView = new ImageView(AppUtils.getContext());
                imageView.setTag(monitorImages.get(i));
                imageView.setOnClickListener(this);
                Picasso.with(AppUtils.getContext()).load(Urls.IMAGE_SERVER_URL + thumbUrl).into(imageView);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AppUtils.dip2px(90), AppUtils.dip2px(60));
                imageView.setLayoutParams(params);
                if (i != monitorImages.size() - 1) {
                    params.setMargins(0, 0, 10, 0);
                }
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                imageGroup.addView(imageView);
            }
        } else {
            scrollView.setVisibility(View.GONE);
        }
    }

    //编辑mark
    @OnClick(btn_edit)
    public void onEdit() {
        Bundle args = new Bundle();
        args.putParcelable("data", cameraBean);
        AppUtils.startActivity(EditMonitorActivity.class, args);
    }

    //上传图像
    @OnClick(R.id.btn_upload)
    public void onUpload() {
        Map<String, String> data = new HashMap<>();
        data.put("imageDir", Urls.SERVER_IMAGE_DIR);
        data.put("id", this.cameraBean.id + "");

        UploadImageMenuDialog dialog = new UploadImageMenuDialog(BaseActivity.currentActivity, R.style.dialog);
        dialog.data = data;
        dialog.show();
    }

    //移动mark
    @OnClick(btn_move)
    public void onMove() {
        TypeEvent.send(TypeEvent.MOVE_MARKER);
    }

    //派发事件，在mapListener中删除mark
    @OnClick(btn_del)
    public void onDelete() {
        System.out.println("del");
        TypeEvent event = new TypeEvent(TypeEvent.DEL_MONITOR);
        event.dispatch();
    }

    //点击图片后显示放大图像
    @Override
    public void onClick(View view) {
        if (view instanceof ImageView) {
            CameraImageBean cameraImageBean = (CameraImageBean) view.getTag();
            int position = monitorImages.indexOf(cameraImageBean);
            Bundle args = new Bundle();
            args.putParcelableArrayList("images", monitorImages);
            args.putInt("position", position);
            args.putParcelable("cameraBean",cameraBean);

            boolean isShowDelBtn = false;
            if (cameraBean.userID == User.id || "admin".equals(User.userType)) {
                isShowDelBtn = true;
            }
            args.putBoolean("isShowDelBtn", isShowDelBtn);
            AppUtils.startActivity(ImageDetailActivity.class, args);
        }
    }

    //删除图片
    @Override
    public void onDrag(final View view, int action) {
        if (action == ImageGroup.DELETE_IMAGE) {
            if (cameraBean.userID == User.id || "admin".equals(User.userType)) {
                DialogUtils.showAlert(BaseActivity.currentActivity, "删除提示", "确定要删除这张图片吗？", "确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                confirmDel(view);
                            }
                        }, "取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                imageGroup.returnPosition(view);
                            }
                        });
            } else {
                imageGroup.returnPosition(view);
            }
        } else {
            imageGroup.returnPosition(view);
        }
    }

    private void confirmDel(View view) {
        imageGroup.removeView(view);
        if (imageGroup.getChildCount() == 0) {
            ((ViewGroup) (imageGroup.getParent())).setVisibility(GONE);
        }
        delImageObserver = new DelObserver();
        CameraImageBean imageBean = (CameraImageBean) view.getTag();
        delImageUrl = imageBean.imageUrl;
        delThumbUrl = imageBean.thumbUrl;
        long id = imageBean.id;
        String sql = SqlFactory.delete(TableName.MONITOR_IMAGE, id);
        HttpMethods.getInstance().getSqlResult(delImageObserver, SqlAction.DELETE, sql);
        delImageObserver.setOnNextListener(this);
    }

    //eventbus 的监听，刷新图像
    @Subscribe
    public void refreshImage(TypeEvent event) {
        if (event.type == TypeEvent.UPLOAD_COMPLETE)
            getMonitorImages();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 和服务器端操作的回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onNext(Object data, Observer observer) {
        if (observer == imageObserver) {
            monitorImages = (ArrayList<CameraImageBean>) data;
            setMonitorImages();
        } else if (observer == delImageObserver) {
            int rows = (int) data;
            if (rows > 0) {
                AppUtils.showToast("删除图片成功");
                //数据库删除后，删除服务器端文件
                delFileObserver = new DelObserver();

                String filePaths = delImageUrl + " " + delThumbUrl;
                HttpMethods.getInstance().delFile(delFileObserver, filePaths);
                delFileObserver.setOnNextListener(this);
            }
        }
    }
}
