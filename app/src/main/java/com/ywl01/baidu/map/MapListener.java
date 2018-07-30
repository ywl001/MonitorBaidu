package com.ywl01.baidu.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.lbsapi.model.BaiduPanoData;
import com.baidu.lbsapi.panoramaview.PanoramaRequest;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.ywl01.baidu.User;
import com.ywl01.baidu.activitys.AddMonitorActivity;
import com.ywl01.baidu.activitys.BaseActivity;
import com.ywl01.baidu.activitys.PanoramaActivity;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.consts.TableName;
import com.ywl01.baidu.events.CameraInfoEvent;
import com.ywl01.baidu.events.MapLevelChangeEvent;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.DelObserver;
import com.ywl01.baidu.net.observers.MarkerObserver;
import com.ywl01.baidu.net.observers.UpdateObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observer;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class MapListener implements
        BaiduMap.OnMapStatusChangeListener,
        BaiduMap.OnMapClickListener,
        BaiduMap.OnMapLongClickListener,
        BaiduMap.OnMarkerClickListener,
        BaseObserver.OnNextListener {

    private MapStatus currentMapStatus;
    private MarkerObserver selectMarkerObserver;
    public static long highLightID;

    private BaiduMap map;
    private Marker currentMarker;
    private Marker prevMarker;
    private boolean isRefresh;

    private boolean isMoveMarker;
    private UpdateObserver moveMarkObserver;

    private DelObserver delMarkerObserver;
    private long delMarkID;

    private Map<Long, Marker> markerLib;
    private boolean isShowPanorama;

    public MapListener(BaiduMap map) {
        this.map = map;
        EventBus.getDefault().register(this);
        markerLib = new HashMap<>();
    }

    //当地图状态变化，如果有marker信息窗口，隐藏
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        CameraInfoEvent event = new CameraInfoEvent(CameraInfoEvent.HIDE_CAMERA_INFO_VIEW);
        event.dispatch();
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    //监听状态变化，更新显示地图级别
    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        System.out.println("map status change....");
        MapLevelChangeEvent event = new MapLevelChangeEvent();
        event.mapZoom = mapStatus.zoom;
        event.dispatch();
    }

    //地图状态改变后，加载范围内mark信息
    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        System.out.println("map status change finish");
        clearOutBoundMark(mapStatus);
        currentMapStatus = mapStatus;
        loadMarks(currentMapStatus.bound, mapStatus.zoom);
    }

    //长按地图，放大到最大级别，添加监控
    @Override
    public void onMapLongClick(LatLng latLng) {
        if (User.id > 0) {
            MapStatus mapStatus = map.getMapStatus();
            float zoom = mapStatus.zoom;
            if (zoom < map.getMaxZoomLevel() - 1) {
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latLng, map.getMaxZoomLevel());
                map.setMapStatus(update);
                AppUtils.showToast("已放大到地图的最大级别");
            } else {
                Bundle args = new Bundle();
                args.putDouble("x", latLng.latitude);
                args.putDouble("y", latLng.longitude);
                AppUtils.startActivity(AddMonitorActivity.class, args);
            }
        }
    }

    //单击mark事件，显示mark信息
    @Override
    public boolean onMarkerClick(Marker marker) {
        currentMarker = marker;
        //移动位置时如果点击到了marker，则取消移动操作
        isMoveMarker = false;

        marker.setToTop();
        //改变被点击mark的icon
        changeClickMarkerSymbol();

        //派发事件，从mainActivity中加载markInfoView
        CameraInfoEvent e = new CameraInfoEvent(CameraInfoEvent.SHOW_CAMERA_INFO_VIEW);
        e.cameraBean = (CameraBean) marker.getExtraInfo().get("data");
        e.dispatch();

        if (highLightID > 0) {
            highLightID = 0;
            loadMarks(currentMapStatus.bound, currentMapStatus.zoom);
        }
        return true;
    }

    //单击地图事件，派发隐藏mark信息窗口，或移动mark点位置
    @Override
    public void onMapClick(LatLng latLng) {
        CameraInfoEvent event = new CameraInfoEvent(CameraInfoEvent.HIDE_CAMERA_INFO_VIEW);
        event.dispatch();

        if (isMoveMarker) {//移动mark位置
            isMoveMarker = false;
            currentMarker.setPosition(latLng);
            CameraBean marker = (CameraBean) currentMarker.getExtraInfo().get("data");
            long id = marker.id;
            Map<String, String> tableData = new HashMap<>();
            tableData.put("x", latLng.latitude + "");
            tableData.put("y", latLng.longitude + "");

            moveMarkObserver = new UpdateObserver();
            String sql = SqlFactory.update(TableName.MONITOR, tableData, id);
            HttpMethods.getInstance().getSqlResult(moveMarkObserver, SqlAction.UPDATE, sql);
            moveMarkObserver.setOnNextListener(this);
        } else if (isShowPanorama) {//显示位置全景图

            double lat = latLng.latitude;
            double lng = latLng.longitude;

            PanoramaRequest panoramaRequest = PanoramaRequest.getInstance(AppUtils.getContext());
            BaiduPanoData panoramaInfo = panoramaRequest.getPanoramaInfoByLatLon(lng, lat);
            if (panoramaInfo.hasStreetPano()) {
                System.out.println("有全景信息");
                Intent intent = new Intent(BaseActivity.currentActivity, PanoramaActivity.class);
                intent.putExtra("lat", latLng.latitude);
                intent.putExtra("lng", latLng.longitude);
                BaseActivity.currentActivity.startActivity(intent);
            } else {
                AppUtils.showToast("该位置没有全景信息");
            }
            isShowPanorama = false;
            //派发事件更改图标
            TypeEvent.send(TypeEvent.CHANGE_PANORAMA_ICON);
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Eventbus 监听事件
    ///////////////////////////////////////////////////////////////////////////

    //移动marker，显示全景图
    @Subscribe
    public void onTypeEvent(TypeEvent event) {
        if(event.type == TypeEvent.SHOW_PANORRMA){
            isShowPanorama = true;
            AppUtils.showToast("请在地图上点击要显示街景的位置");
        } else if (event.type == TypeEvent.MOVE_MARKER) {
            AppUtils.showToast("请在要移动的位置上单击");
            isMoveMarker = true;
        }
    }

    //删除mark
    @Subscribe
    public void delMarker(TypeEvent event) {
        if (event.type == TypeEvent.DEL_MONITOR) {
            DialogUtils.showAlert(BaseActivity.currentActivity,
                    "删除提示",
                    "确定要删除该监控点吗？",
                    "确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            confirmDel();
                            dialogInterface.dismiss();
                        }
                    },
                    "取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
        }
    }

    private void confirmDel() {
        currentMarker.remove();//视图中删除
        delMarkerObserver = new DelObserver();
        CameraBean marker = (CameraBean) currentMarker.getExtraInfo().get("data");
        delMarkID = marker.id;
        String sql = SqlFactory.delete(TableName.MONITOR, delMarkID);
        HttpMethods.getInstance().getSqlResult(delMarkerObserver, SqlAction.DELETE, sql);
        delMarkerObserver.setOnNextListener(this);
    }

    //通过发送事件来全部刷新地图（添加监控后、编辑监控信息后）
    @Subscribe
    public void refreshAll(TypeEvent event) {
        if(event.type == TypeEvent.REFRESH_MARKERS){
            isRefresh = true;
            loadMarks(currentMapStatus.bound, currentMapStatus.zoom);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // onNext 回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onNext(Object data, Observer observer) {
        //加载marker流程：从服务器端获取json数据，在markObserver中封装成List<MarkOptions>,通过clearExistsOptions清除已经在地图范围的数据
        //把剩余的数据添加到视图中
        if (observer == selectMarkerObserver) {
            //服务器端获取数据后转化为options集合
            List<MarkerOptions> optionses = (List<MarkerOptions>) data;

            if (isRefresh) {
                map.clear();
                isRefresh = false;
            } else {
                //去除视图中已经存在的数据
                clearExistsOptions(optionses);
            }
            //添加新数据到视图中，集合中
            int size = optionses.size();
            for (int i = 0; i < size; i++) {
                Marker marker = (Marker) map.addOverlay(optionses.get(i));
                CameraBean mb = (CameraBean) marker.getExtraInfo().get("data");
                if (mb.id == highLightID) {
                    marker.setToTop();
                }
                markerLib.put(mb.id, marker);//保存当前地图内的marker
            }
        } else if (observer == moveMarkObserver) {
            int rows = (int) data;
            if (rows > 0) {
                AppUtils.showToast("移动位置成功");
            }
        } else if (observer == delMarkerObserver) {
            int rows = (int) data;
            if (rows > 0) {
                AppUtils.showToast("删除监控点成功");
                //删除监控点后，清除markinfo窗口
                CameraInfoEvent cameraInfoEvent = new CameraInfoEvent(CameraInfoEvent.HIDE_CAMERA_INFO_VIEW);
                cameraInfoEvent.dispatch();
                // 删除和该监控点关联的图像，数据库通过触发器删除。
            }
        }
    }

    /**
     * 更新当前地图范围从数据库加载该区域监控信息
     *
     * @param bound    地图范围
     * @param mapLevel 地图级别
     */
    private void loadMarks(LatLngBounds bound, float mapLevel) {
        selectMarkerObserver = new MarkerObserver();
        String sql = SqlFactory.selectMarkersByBound(bound, mapLevel);
        HttpMethods.getInstance().getSqlResult(selectMarkerObserver, SqlAction.SELECT, sql);
        selectMarkerObserver.setOnNextListener(this);
    }

    //清除数据中已经显示在地图范围内的options
    private void clearExistsOptions(List<MarkerOptions> optionses) {
        for (int i = optionses.size() - 1; i >= 0; i--) {
            MarkerOptions option = optionses.get(i);
            CameraBean m1 = (CameraBean) option.getExtraInfo().get("data");
            if (markerLib.containsKey(m1.id)) {
                optionses.remove(i);
            }
        }
    }

    //清除不再范围内的marker，数组删除一定要反向遍历
    private void clearOutBoundMark(MapStatus mapStatus) {
        LatLngBounds bound = mapStatus.bound;
        float zoom = mapStatus.zoom;
        Iterator<Long> iterator = markerLib.keySet().iterator();
        while (iterator.hasNext()) {
            Marker marker = markerLib.get(iterator.next());
            CameraBean cameraBean = (CameraBean) marker.getExtraInfo().get("data");
            if (!bound.contains(marker.getPosition()) || cameraBean.displayLevel > zoom) {
                marker.remove();
                iterator.remove();
            }
        }
    }

    //改变被点击marker的图标
    private void changeClickMarkerSymbol() {
        Bundle bundle = currentMarker.getExtraInfo();

        CameraBean monitor = (CameraBean) bundle.get("data");
        String currentType = monitor.type;

        BitmapDescriptor bitmapDesc = null;

        if (prevMarker != null) {
            CameraBean prevMonitor = (CameraBean) prevMarker.getExtraInfo().get("data");
            String prevType = prevMonitor.type;
            int isRunning = prevMonitor.isRunning;

            if (isRunning == 1) {
                bitmapDesc = SymbolManager.getIconByName(prevType);
            } else {
                bitmapDesc = SymbolManager.getIconByName(prevType + "_error");
            }
            prevMarker.setIcon(bitmapDesc);
        }
//        ArrayList<BitmapDescriptor> icons = new ArrayList<>();
//        icons.add(SymbolManager.getIconByName(currentType + "_press"));
//        icons.add(SymbolManager.getIconByName(currentType));

        bitmapDesc = SymbolManager.getIconByName(currentType + "_press");

        currentMarker.setIcon(bitmapDesc);

        prevMarker = currentMarker;
    }
}
