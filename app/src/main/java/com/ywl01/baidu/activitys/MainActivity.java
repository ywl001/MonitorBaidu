package com.ywl01.baidu.activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.ywl01.baidu.AppUpdate;
import com.ywl01.baidu.R;
import com.ywl01.baidu.User;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.consts.RequestCode;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.consts.TableName;
import com.ywl01.baidu.events.CameraInfoEvent;
import com.ywl01.baidu.events.CamerasEvent;
import com.ywl01.baidu.events.MapLevelChangeEvent;
import com.ywl01.baidu.events.ProgressEvent;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.events.UploadImageStartEvent;
import com.ywl01.baidu.map.Location;
import com.ywl01.baidu.map.MapListener;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.ProgressRequestBody;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.InsertObserver;
import com.ywl01.baidu.net.observers.UploadObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;
import com.ywl01.baidu.utils.ImageUtils;
import com.ywl01.baidu.utils.PhotoUtils;
import com.ywl01.baidu.utils.SPUtils;
import com.ywl01.baidu.views.CameraInfoView;
import com.ywl01.baidu.views.LoginDialog;
import com.ywl01.baidu.views.SearchView;
import com.ywl01.baidu.views.adapter.CameraListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;

public class MainActivity extends BaseActivity implements
        BaseObserver.OnNextListener,
        AdapterView.OnItemClickListener {

    private boolean isLogin;

    @Bind(R.id.main_container)
    LinearLayout mainContainer;

    @Bind(R.id.btn_mapType)
    ImageView imgSwitchMap;

    @Bind(R.id.map_container)
    RelativeLayout mapContainer;

    @Bind(R.id.btn_container)
    LinearLayout btnContainer;

    @Bind(R.id.search_view)
    SearchView searchView;

    @Bind(R.id.list_view)
    ListView listView;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.tv_level)
    TextView tvLevel;

    @Bind(R.id.tv_login_info)
    TextView tvLoginInfo;

    @Bind(R.id.iv_login)
    ImageView btnLogin;

    @Bind(R.id.btn_panorama)
    ImageView btnPanorama;

    @Bind(R.id.tv_search_title)
    TextView tvSearchTitle;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    private BaiduMap baiduMap;
    private Location location;
    private CameraInfoView cameraInfoView;
    private UploadImageStartEvent uploadImageEvent;
    private UploadObserver uploadObserver;
    private InsertObserver insertObserver;
    private MapView mapView;
    private List<CameraBean> cameraBeans;
    private long exitTime;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initMapView();
        initUserView();
        drawerLayout.setScrimColor(Color.TRANSPARENT);
    }

    private void initMapView() {
        BaiduMapOptions options = new BaiduMapOptions();
        options.rotateGesturesEnabled(false);
        options.overlookingGesturesEnabled(false);
        options.zoomGesturesEnabled(true);
        mapView = new MapView(this, options);
        mapContainer.addView(mapView, 0);
        mapView.removeViewAt(1);//隐藏百度图标
        mapView.showScaleControl(false);//隐藏比例尺
        mapView.showZoomControls(false);//隐藏缩放控件
    }

    private void initUserView() {
        long userID = (long) SPUtils.get(this, "userID", 0L);
        if (userID > 0) {
            User.id = userID;
            User.userName = (String) SPUtils.get(this, "userName", "");
            User.userType = (String) SPUtils.get(this, "userType", "");
            User.realName = (String) SPUtils.get(this, "realName", "");
            tvLoginInfo.setText("欢迎你，" + User.realName);
            isLogin = true;
            btnLogin.setImageResource(R.drawable.exitlogin);
        }
    }

    @Override
    protected void initData() {
        boolean isConnect = AppUtils.isNetConnect(this);
        if (!isConnect) {
            AppUtils.showToast("无网络可以使用，请连接好网络后再运行");
        }

        //从服务器端获取更新信息
        AppUpdate appUpdate = new AppUpdate();

        //获取手机屏幕的宽高信息
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        listView.setOnItemClickListener(this);

        //设置地图监听器
        baiduMap = mapView.getMap();
        MapListener mapListener = new MapListener(baiduMap);
        baiduMap.setOnMapStatusChangeListener(mapListener);
        baiduMap.setOnMapLongClickListener(mapListener);
        baiduMap.setOnMarkerClickListener(mapListener);
        baiduMap.setOnMapClickListener(mapListener);

        //初始化定位
        location = new Location(this, baiduMap);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                exitApp();
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Subscribe
    public void uploadImage(final UploadImageStartEvent event) {
        uploadImageEvent = event;
        if (event.type == UploadImageStartEvent.FROM_PHOTOS)
            PhotoUtils.selectPhoto(this);
        else if (event.type == UploadImageStartEvent.FROM_CAMERS)
            PhotoUtils.takePhoto(this);
    }

    //系统返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RequestCode.SELECT_PHOTO) {
                // 被选中文件的Uri
                Uri uri = data.getData();
                File file = new File(getPathByUri(uri));
                Bitmap bitmap = ImageUtils.getScaleBitmap(file.getPath());
                File tempFile = ImageUtils.saveBitmap(bitmap, "temp");
                uploadFile(tempFile);
            } else if (requestCode == RequestCode.TAKE_PHOTO) {
                Bitmap bm = ImageUtils.getScaleBitmap(PhotoUtils.tempFile.getPath());
                File tempFile = ImageUtils.saveBitmap(bm, "temp");
                uploadFile(tempFile);
            } else if (requestCode == RequestCode.SEARCH_RESULT) {
                List camreas = data.getParcelableArrayListExtra("cameras");
                System.out.println("cameras size" + camreas.size());
                CamerasEvent event = new CamerasEvent();
                event.cameraBeans = camreas;
                event.dispatch();
            }
        }
    }

    private String getPathByUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }

    private void uploadFile(File file) {
        //传递服务器端存储图片文件的目录
        String server_image_dir = uploadImageEvent.IMAGE_DIR;
        RequestBody fileDir = RequestBody.create(MultipartBody.FORM, server_image_dir);
        //上传文件的包装Filedata为php服务器端_FILE[Filedata]
        ProgressRequestBody requestFile = new ProgressRequestBody(file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("Filedata", file.getName(), requestFile);

        uploadObserver = new UploadObserver();

        HttpMethods.getInstance().uploadFile(uploadObserver, fileDir, body);
        uploadObserver.setOnNextListener(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // onNext的回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onNext(Object data, Observer observer) {
        if (observer == uploadObserver) {
            String returnData = (String) data;
            String imgUrl = returnData.substring(14);
            String[] temp = imgUrl.split("\\.");
            String thumbUrl = temp[0] + "_thumb.jpg";

            insertObserver = new InsertObserver();
            Map<String, String> tableData = new HashMap<String, String>();
            String id = uploadImageEvent.id;

            tableData.put("monitorID", id);
            tableData.put("imageUrl", imgUrl);
            tableData.put("thumbUrl", thumbUrl);
            tableData.put("insertUser", User.id + "");
            tableData.put("insertTime", "now()");
            String sql = SqlFactory.insert(TableName.MONITOR_IMAGE, tableData);
            HttpMethods.getInstance().getSqlResult(insertObserver, SqlAction.INSERT, sql);

            insertObserver.setOnNextListener(this);
        } else if (observer == insertObserver) {
            Long returnData = (Long) data;
            if (returnData > 0) {
                AppUtils.showToast("上传图片成功");
                //派发事件，让cameraInfoView刷新图片
                TypeEvent.send(TypeEvent.UPLOAD_COMPLETE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // listview item 点击回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        drawerLayout.closeDrawers();
        CameraBean cameraBean = cameraBeans.get(position);
        LatLng latLng = new LatLng(cameraBean.x, cameraBean.y);

        //地图中心移动到该位置
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, cameraBean.displayLevel);
        baiduMap.animateMapStatus(mapStatusUpdate, 500);//动画效果
        //高亮显示该camera
        MapListener.highLightID = cameraBean.id;
        TypeEvent.send(TypeEvent.REFRESH_MARKERS);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 各种点击事件
    ///////////////////////////////////////////////////////////////////////////
    //放大地图
    @OnClick(R.id.btn_zoomIn)
    public void onZoomIn() {
        MapStatus currentMapStatus = baiduMap.getMapStatus();
        float zoomLevel = currentMapStatus.zoom;

        if (zoomLevel < baiduMap.getMaxZoomLevel()) {
            MapStatusUpdate update = MapStatusUpdateFactory.zoomIn();
            //此次以动画方式更新会出发status change，
            //baiduMap.setMapStatus(update);
            baiduMap.animateMapStatus(update);

        } else {
            AppUtils.showToast("已经到最大地图级别");
        }
    }

    //缩小地图
    @OnClick(R.id.btn_zoomOut)
    public void onZoomOut() {
        MapStatus currentMapStatus = baiduMap.getMapStatus();
        float zoomLevel = currentMapStatus.zoom;

        if (zoomLevel > baiduMap.getMinZoomLevel()) {
            MapStatusUpdate update = MapStatusUpdateFactory.zoomOut();
            baiduMap.animateMapStatus(update);
        } else {
            AppUtils.showToast("已经到最小地图级别");
        }
    }

    //切换地图
    @OnClick(R.id.btn_mapType)
    public void onSwitchMap() {
        int currentMapType = baiduMap.getMapType();
        if (currentMapType == BaiduMap.MAP_TYPE_NORMAL) {
            baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            imgSwitchMap.setImageResource(R.drawable.imgmap);
        } else {
            baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            imgSwitchMap.setImageResource(R.drawable.vecmap);
        }
    }

    //下载地图
    @OnClick(R.id.btn_down_map)
    public void onDownLoadMap() {
        Intent intent = new Intent(this, OfflineActivity.class);
        startActivity(intent);
    }

    //定位
    @OnClick(R.id.btn_show_location)
    public void onShowLocation() {
        System.out.println("开始手动定位");
        location.requestLocation();
        AppUtils.playSound(R.raw.click);
    }

    //搜索
    @OnClick(R.id.btn_search)
    public void onSearch() {
        btnContainer.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
    }

    @OnLongClick(R.id.btn_search)
    public boolean onSearch2(){
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, RequestCode.SEARCH_RESULT);
        return true;
    }

    //登陆
    @OnClick(R.id.iv_login)
    public void onLogin() {
        if (isLogin) {
            DialogUtils.showAlert(this, "提示信息", "确定要退出吗？", "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitLogin();
                }
            }, "取消", null);

        } else {
            LoginDialog loginDialog = new LoginDialog(this, R.style.dialog);
            loginDialog.show();
        }
    }

    private void exitLogin() {
        btnLogin.setImageResource(R.drawable.login);
        tvLoginInfo.setText("");
        SPUtils.remove(this, "userID");
        SPUtils.remove(this, "userName");
        SPUtils.remove(this, "userType");
        SPUtils.remove(this, "realName");
        User.id = 0L;
        User.realName = null;
        User.userType = null;
        User.userName = null;

        //退出登录后刷新marker
        TypeEvent.send(TypeEvent.REFRESH_MARKERS);
        //如果有cameraInfo，删除
        CameraInfoEvent event = new CameraInfoEvent(CameraInfoEvent.HIDE_CAMERA_INFO_VIEW);
        event.dispatch();

        isLogin = false;
    }

    //显示全景信息
    @OnClick(R.id.btn_panorama)
    public void showPanoramaView() {
        TypeEvent.send(TypeEvent.SHOW_PANORRMA);
        btnPanorama.setImageResource(R.drawable.panoramic_press);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Eventbus监听的事件
    ///////////////////////////////////////////////////////////////////////////

    //显示或隐藏Camerainfo窗口
    @Subscribe
    public void onCameraInfoView(CameraInfoEvent evnet) {
        if (evnet.type == CameraInfoEvent.SHOW_CAMERA_INFO_VIEW) {
            CameraBean cameraBean = evnet.cameraBean;

            if (cameraInfoView == null)
                cameraInfoView = new CameraInfoView(this);

            cameraInfoView.setData(cameraBean);

            if (cameraInfoView.getParent() == null) {
                mainContainer.addView(cameraInfoView);
            }
            //动画效果
            //TranslateAnimation animation = new TranslateAnimation(0, 0, 0, cameraInfoView.getHeight());
//        animation.setDuration(500);
//        cameraInfoView.startAnimation(animation);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setDuration(500);
            cameraInfoView.startAnimation(alphaAnimation);
        } else if (evnet.type == CameraInfoEvent.HIDE_CAMERA_INFO_VIEW) {
            if (cameraInfoView != null && cameraInfoView.getParent() != null)
                mainContainer.removeView(cameraInfoView);
        }
    }

    //显示搜索结果
    @Subscribe
    public void showSearchCamears(CamerasEvent event) {
        this.cameraBeans = event.cameraBeans;
        CameraListAdapter adapter = new CameraListAdapter(listView, this.cameraBeans);
        tvSearchTitle.setText("搜索到" + this.cameraBeans.size() + "个结果：");
        listView.setAdapter(adapter);
        searchView.setVisibility(View.GONE);
        btnContainer.setVisibility(View.VISIBLE);
        drawerLayout.openDrawer(Gravity.LEFT, true);

        hideSoftkey();
    }

    //显示当前地图的级别
    @Subscribe
    public void showLevel(MapLevelChangeEvent event) {
        float mapZoom = event.mapZoom;
        tvLevel.setText(mapZoom + "");
    }

    //改变全景图标,登录，显示按钮组
    @Subscribe
    public void onTypeEvent(TypeEvent event) {
        if (event.type == TypeEvent.CHANGE_PANORAMA_ICON)
            btnPanorama.setImageResource(R.drawable.panoramic);
        else if (event.type == TypeEvent.LOGIN) {
            tvLoginInfo.setText("欢迎你，" + User.realName);
            btnLogin.setImageResource(R.drawable.exitlogin);
            isLogin = true;
        } else if (event.type == TypeEvent.SHOW_BTN_CONTAINER) {
            btnContainer.setVisibility(View.VISIBLE);
        }
    }

    //显示上传图片进度
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgress(ProgressEvent event) {
        int percentage = (int) (100 * event.progress / event.total);
        System.out.println(percentage);
        progressBar.setProgress(percentage);
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);
        if (percentage == 100) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
