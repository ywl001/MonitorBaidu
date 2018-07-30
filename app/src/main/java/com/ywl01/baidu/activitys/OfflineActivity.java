package com.ywl01.baidu.activitys;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ywl01.baidu.R;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;
import com.ywl01.baidu.utils.MPermissionUtils;

import java.util.ArrayList;

public class OfflineActivity extends BaseActivity implements MKOfflineMapListener {

    private int requestCode = 100;

    private EditText et_cityName;

    private MKOfflineMap mOffline;
    private ArrayList<MKOLUpdateElement> localMapList;
    private Button btn_download;

    private LocalMapAdapter lAdapter;
    private int cityID;
    private String cityName;

    @Override
    protected void initView() {
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_offline_map);
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        et_cityName = (EditText) findViewById(R.id.et_city);
        btn_download = (Button) findViewById(R.id.btn_download);
        // 获取已下过的离线地图信息
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }

        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        lAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(lAdapter);
    }

    private void checkPremission() {
        if (ContextCompat.checkSelfPermission(AppUtils.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(AppUtils.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //有权限，继续执行
            startDownload();
        } else {
            //没有申请权限
            MPermissionUtils.requestPermissionsResult(
                    BaseActivity.currentActivity,
                    requestCode,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    new MPermissionUtils.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            //申请权限成功
                            startDownload();
                        }

                        @Override
                        public void onPermissionDenied() {
                            //申请权限没有获准
                            MPermissionUtils.showTipsDialog(BaseActivity.currentActivity);
                        }
                    }
            );
        }
    }

    @Override
    protected void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("离线地图下载");
    }

    /**
     * 搜索离线需市
     *
     * @param view
     */
    public void search(View view) {
        ArrayList<MKOLSearchRecord> records = mOffline.searchCity(et_cityName.getText().toString());
        if (records == null || records.size() != 1) {
            AppUtils.showToast("查询结果为空，或查询结果太多");
            return;
        }
        cityID = records.get(0).cityID;
        cityName = records.get(0).cityName;
        AppUtils.showToast("已经查询到该城市编号，请点击下载按钮下载");
        btn_download.setEnabled(true);
        btn_download.setBackgroundResource(R.drawable.download);
        hideSoftkey();
//		btn_download.setAlpha(1.0f);
    }

    public void download(View view) {
        checkPremission();

    }

    private void startDownload() {
        mOffline.start(cityID);
//        AppUtils.showToast("开始下载" + cityName + "离线地图: ");
        updateView();
    }

    /**
     * 更新状态显示
     */
    public void updateView() {
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        lAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    updateView();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
            default:
                break;
        }
    }

    public String formatDataSize(int size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    /**
     * 离线地图管理列表适配器
     */
    public class LocalMapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            System.out.println(localMapList.size());
            return localMapList.size();
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            view = View.inflate(OfflineActivity.this, R.layout.offline_localmap_list, null);
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            Button remove = (Button) view.findViewById(R.id.remove);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView update = (TextView) view.findViewById(R.id.update);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.update) {
                update.setText("可更新");
            } else {
                update.setText("最新");
            }
            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    DialogUtils.showAlert(OfflineActivity.this, "删除提示：", "确定要删除吗？", "确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onRemove(e);
                                    dialogInterface.dismiss();
                                }
                            }, "取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                }
            });
        }

        private void onRemove(MKOLUpdateElement e) {
            mOffline.remove(e.cityID);
            updateView();
        }

        @Override
        public Object getItem(int index) {
            return localMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }
    }
}
