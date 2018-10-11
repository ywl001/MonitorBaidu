package com.ywl01.baidu.activitys;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ywl01.baidu.R;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.beans.CameraImageBean;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.consts.TableName;
import com.ywl01.baidu.consts.Urls;
import com.ywl01.baidu.events.CameraInfoEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.DelObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;
import com.ywl01.baidu.views.ImagePageViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;


/**
 * Created by ywl01 on 2017/3/14.
 */

public class ImageDetailActivity extends BaseActivity implements ViewPager.OnPageChangeListener{

    private List<CameraImageBean> images;
    private int position;
    private boolean isShowDelBtn;
    private CameraBean cameraBean;

    private String delImageUrl;
    private String delThumbUrl;

    @BindView(R.id.image_pager)
    ViewPager imagePager;

    @BindView(R.id.btn_del)
    ImageView btnDel;

    private PagerAdapter pagerAdapter;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);


        Bundle bundle = getIntent().getExtras();
        images = bundle.getParcelableArrayList("images");
        position = bundle.getInt("position");
        isShowDelBtn = bundle.getBoolean("isShowDelBtn");
        cameraBean = bundle.getParcelable("cameraBean");

        btnDel.setVisibility(isShowDelBtn ? View.VISIBLE : View.GONE);

        pagerAdapter = new ImagePageAdapter(images);
        imagePager.setAdapter(pagerAdapter);
        imagePager.setCurrentItem(position);
        imagePager.addOnPageChangeListener(this);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @OnClick(R.id.btn_del)
    public void del() {
        DialogUtils.showAlert(BaseActivity.currentActivity, "删除提示", "确定要删除这张图片吗？", "确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmDel();
                    }
                }, "取消", null);

    }

    private void confirmDel() {

        DelObserver delImageObserver = new DelObserver();

        CameraImageBean imageBean = images.get(position);
        delImageUrl = imageBean.imageUrl;
        delThumbUrl = imageBean.thumbUrl;
        long id = imageBean.id;
        String sql = SqlFactory.delete(TableName.MONITOR_IMAGE, id);
        HttpMethods.getInstance().getSqlResult(delImageObserver, SqlAction.DELETE, sql);

        delImageObserver.setOnNextListener(new BaseObserver.OnNextListener() {
            @Override
            public void onNext(Object data, Observer observer) {
                int rows = (int) data;
                if (rows > 0) {
                    AppUtils.showToast("删除图片成功");

                    images.remove(position);

                    if (images.size() == 0) {
                        finish();
                    } else
                        pagerAdapter.notifyDataSetChanged();

                    //数据库删除后，删除服务器端文件
                    DelObserver delFileObserver = new DelObserver();

                    String filePaths = delImageUrl + " " + delThumbUrl;
                    HttpMethods.getInstance().delFile(delFileObserver, filePaths);
                    delFileObserver.setOnNextListener(this);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //派发事件，从mainActivity中加载markInfoView
        CameraInfoEvent e = new CameraInfoEvent(CameraInfoEvent.SHOW_CAMERA_INFO_VIEW);
        e.cameraBean = cameraBean;
        e.dispatch();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 实现viewpager的OnPageChangeListener，用以从外部获取position
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    ///////////////////////////////////////////////////////////////////////////
    // adapter的实现
    ///////////////////////////////////////////////////////////////////////////
    class ImagePageAdapter extends PagerAdapter {

        private List<CameraImageBean> imageUrls;

        public ImagePageAdapter(List<CameraImageBean> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImagePageViewHolder imagePageViewHolder = new ImagePageViewHolder();
            View view = imagePageViewHolder.getRootView();
            imagePageViewHolder.setData(Urls.IMAGE_SERVER_URL + imageUrls.get(position).imageUrl);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


}
