package com.ywl01.baidu.activitys;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ywl01.baidu.R;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.events.CameraInfoEvent;
import com.ywl01.baidu.events.GetAngleEvent;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.UpdateObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.SPUtils;
import com.ywl01.baidu.views.CompassDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

import static com.ywl01.baidu.R.id.cb_isRunning;
import static com.ywl01.baidu.utils.SPUtils.get;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class EditMonitorActivity extends BaseActivity implements View.OnClickListener, BaseObserver.OnNextListener {
    private Context context;
    private CameraBean cameraBean;

    @BindView(R.id.et_number)
    EditText etNumber;

    @BindView(R.id.et_type)
    EditText etType;

    @BindView(R.id.et_name)
    EditText etName;

    @BindView(R.id.et_owner)
    EditText etOwner;

    @BindView(R.id.et_level)
    EditText etLevel;

    @BindView(R.id.et_angle)
    EditText etAngle;

    @BindView(R.id.et_telephone)
    EditText etTelephone;

    @BindView(cb_isRunning)
    CheckBox cbIsRunning;

    @BindView(R.id.ll_rotate_group)
    LinearLayout rotateGroup;


    private boolean isTypeChange;
    private boolean isRunningChange;
    private boolean isAngleChange;

    @Override
    protected void initView() {
        setContentView(R.layout.edit_monitor);
        ButterKnife.bind(this);
        etType.setOnClickListener(this);
        etAngle.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        cameraBean = (CameraBean) data.get("data");

        //枪机、电警、卡口、社会监控显示旋转方向
        if ("枪机".equals(cameraBean.type) || "社会监控".equals(cameraBean.type) || "卡口".equals(cameraBean.type) || "电警".equals(cameraBean.type)) {
            rotateGroup.setVisibility(View.VISIBLE);
        }

        //给控件赋值
        etNumber.setText(checkStr(cameraBean.monitorID));
        etName.setText(checkStr(cameraBean.name));
        etOwner.setText(checkStr(cameraBean.owner));
        etTelephone.setText(checkStr(cameraBean.telephone));
        etAngle.setText(cameraBean.angle + "");

        etType.setText(cameraBean.type);
        etLevel.setText(cameraBean.displayLevel + "");
        cbIsRunning.setChecked(cameraBean.isRunning == 1 ? true : false);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    @OnClick(R.id.btn_getName)
    public void onGetName() {
        String name = (String) get(this, "name", "");
        etName.setText(name);
    }

    @OnClick(R.id.btn_getOwner)
    public void onGetOwner() {
        String owner = (String) get(this, "owner", "");
        etOwner.setText(owner);
    }

    @OnClick(R.id.btn_getPhone)
    public void onGetTelephone() {
        String telephone = (String) get(this, "telephone", "");
        etTelephone.setText(telephone);
    }

    @OnClick(R.id.btn_cancel)
    public void onCancel() {
        finish();
    }

    @OnClick(R.id.btn_submit)
    public void onSubmit() {

        String type = etType.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String owner = etOwner.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();
        String monitorID = etNumber.getText().toString().trim();
        int isRunning = cbIsRunning.isChecked() ? 1 : 0;
        String displayLevel = etLevel.getText().toString().trim();
        String angle = etAngle.getText().toString().trim();

        SPUtils.put(this, "name", name);
        SPUtils.put(this,"owner",owner);
        SPUtils.put(this,"telephone",telephone);

        isTypeChange = !(cameraBean.type.equals(type));
        isRunningChange = !(cameraBean.isRunning == isRunning);
        isAngleChange = !(angle.equals(cameraBean.angle));
        boolean isMonitorIDChange = !(monitorID.equals(cameraBean.monitorID));
        boolean isNameChange = !(name.equals(cameraBean.name));
        boolean isOwnerChange = !(owner.equals(cameraBean.owner));
        boolean isTelephoneChange = !(telephone.equals(cameraBean.telephone));
        boolean isLevelChange = !(cameraBean.displayLevel == Float.parseFloat(displayLevel));

        Map<String, String> tableData = new HashMap<>();
        if (isMonitorIDChange){
            tableData.put("monitorID", monitorID);
            cameraBean.monitorID = monitorID;
        }
        if (isTypeChange){
            tableData.put("type", type);
            cameraBean.type = type;
        }
        if (isNameChange){
            tableData.put("name", name);
            cameraBean.name = name;
        }
        if (isOwnerChange){
            tableData.put("owner", owner);
            cameraBean.owner = owner;
        }
        if (isTelephoneChange){
            tableData.put("telephone", telephone);
            cameraBean.telephone = telephone;
        }
        if (isLevelChange){
            tableData.put("displayLevel", displayLevel);
        }
        if (isRunningChange){
            tableData.put("isRunning", isRunning + "");
        }
        if (isAngleChange) {
            tableData.put("angle", angle);
        }

        if(tableData.isEmpty()){
            finish();
            return;
        }

        UpdateObserver observer = new UpdateObserver();
        String sql = SqlFactory.update("monitor", tableData, cameraBean.id);
        HttpMethods.getInstance().getSqlResult(observer, SqlAction.UPDATE, sql);
        observer.setOnNextListener(this);
        finish();
    }

    //显示选择监控类型对话框
    private void showSelectTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置标题
        builder.setTitle(AppUtils.getResString(R.string.dialog_select_camera_type_title));
        //设置选项
        final String[] items = AppUtils.getResArray(R.array.cameraType);
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                etType.setText(items[which]);
                if(items[which].equals("枪机") || items[which].equals("社会监控") || items[which].equals("卡口") || items[which].equals("电警")){
                    rotateGroup.setVisibility(View.VISIBLE);
                }else{
                    rotateGroup.setVisibility(View.GONE);
                }
            }
        });

        builder.show();
    }

    @Override
    public void onNext(Object data, Observer observer) {
        int rows = (int) data;
        if (rows > 0) {
            CameraInfoEvent event = new CameraInfoEvent(CameraInfoEvent.SHOW_CAMERA_INFO_VIEW);
            event.cameraBean = cameraBean;
            event.dispatch();
            if (isTypeChange || isRunningChange || isAngleChange){
//                RefreshMarkEvent refreshMarkEvent = new RefreshMarkEvent();
//                refreshMarkEvent.dispatch();
                TypeEvent.send(TypeEvent.REFRESH_MARKERS);
            }
        }
    }

    private String checkStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        } else if (str.toLowerCase().equals("null")) {
            return "";
        } else
            return str;
    }

    @Override
    public void onClick(View v) {
        if (etType == v) {
            showSelectTypeDialog();
        }else if(etAngle == v){
            showSelectAngleDialog();
        }
    }

    private void showSelectAngleDialog() {
        CompassDialog dialog = new CompassDialog(BaseActivity.currentActivity,R.style.dialog);
        dialog.show();
        int angle = (int) (cameraBean.angle - 90);
        if(angle < 0)
            angle += 360;
        dialog.setInitAngle(angle);
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
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void getAngle(GetAngleEvent event){
        int angle = event.angle + 90;
        if(angle >= 360){
            angle = angle - 360;
        }
        etAngle.setText(angle + "");
    }
}
