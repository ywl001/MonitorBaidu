package com.ywl01.baidu.activitys;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ywl01.baidu.R;
import com.ywl01.baidu.User;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.events.GetAngleEvent;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.InsertObserver;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.utils.SPUtils;
import com.ywl01.baidu.views.CompassDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

import static com.ywl01.baidu.R.id.cb_isRunning;
import static com.ywl01.baidu.utils.SPUtils.get;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class AddMonitorActivity extends BaseActivity implements BaseObserver.OnNextListener,View.OnClickListener {
    private Context context;

    @Bind(R.id.et_number)
    EditText etNumber;

    @Bind(R.id.et_type)
    EditText etType;

    @Bind(R.id.et_name)
    EditText etName;

    @Bind(R.id.et_owner)
    EditText etOwner;

    @Bind(R.id.et_angle)
    EditText etAngle;

    @Bind(R.id.et_level)
    EditText etLevel;

    @Bind(R.id.et_telephone)
    EditText etTelephone;

    @Bind(cb_isRunning)
    CheckBox cbIsRunning;

    @Bind(R.id.tv_title)
    TextView titleTextView;

    @Bind(R.id.ll_rotate_group)
    LinearLayout rotateGroup;

    @Override
    protected void initView() {
        setContentView(R.layout.add_monitor);
        ButterKnife.bind(this);
        etType.setOnClickListener(this);
        etAngle.setOnClickListener(this);
        etNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
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
        String angle = etAngle.getText().toString().trim();
        String monitorID = etNumber.getText().toString().trim();
        int isRunning = cbIsRunning.isChecked() ? 1 : 0;
        String displayLevel = etLevel.getText().toString().trim();

//        if ("".equals(name)) {
//            AppUtils.showToast("监控名称不能为空");
//            return;
//        }
        SPUtils.put(this, "name", name);
        SPUtils.put(this,"owner",owner);
        SPUtils.put(this,"telephone",telephone);

        Map<String, String> tableData = new HashMap<>();
        tableData.put("monitorID", monitorID);
        tableData.put("type", type);
        tableData.put("name", name);
        tableData.put("owner", owner);
        tableData.put("telephone", telephone);
        if("枪机".equals(type) || "社会监控".equals(type) || "卡口".equals(type) || "电警".equals(type)){
            tableData.put("angle", angle);
        }else{
            tableData.put("angle", "0");
        }

        tableData.put("userID", User.id + "");
        tableData.put("displayLevel", displayLevel);
        tableData.put("isRunning", isRunning + "");
        tableData.put("x", data.getDouble("x") + "");
        tableData.put("y", data.getDouble("y") + "");

        InsertObserver observer = new InsertObserver();
        String sql = SqlFactory.insert("monitor", tableData);
        HttpMethods.getInstance().getSqlResult(observer, SqlAction.INSERT,sql);
        observer.setOnNextListener(this);
        finish();
    }

    //显示选择监控类型对话框
    private void showSelectTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置标题
        builder.setTitle("请选择监控类别");
        //设置选项
        final String[] items = {"枪机", "球机", "卡口","电警","全景","高空瞭望", "社会监控"};
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String selectType = items[which];
                etType.setText(selectType);
                if(selectType.equals("枪机") || selectType.equals("社会监控") || selectType.equals("卡口" ) || selectType.equals("电警")){
                    rotateGroup.setVisibility(View.VISIBLE);
                }else{
                    rotateGroup.setVisibility(View.GONE);
                }

                if("卡口".equals(selectType)){
                    etLevel.setText("13");
                } else if ("全景".equals(selectType) || "高空瞭望".equals(selectType)) {
                    etLevel.setText("12");
                } else if ("社会监控".equals(selectType)) {
                    etLevel.setText("18");
                }
            }
        });

        builder.show();
    }

    private void showSelectAngleDialog() {
        CompassDialog dialog = new CompassDialog(BaseActivity.currentActivity,R.style.dialog);
        dialog.show();
        dialog.setInitAngle(270);
    }


    @OnClick(R.id.btn_getName)
    public void onGetName() {
        String name = (String) SPUtils.get(this, "name", "");
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

    @Override
    public void onNext(Object data, Observer observer) {
        long returnID = (long) data;
        if (returnID > 0) {
            AppUtils.showToast("插入监控点成功");
            System.out.println("插入监控点成功");

            TypeEvent.send(TypeEvent.REFRESH_MARKERS);
        }
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

    @Override
    public void onClick(View v) {
        if (etType == v) {
            showSelectTypeDialog();
        }else if(etAngle == v){
            showSelectAngleDialog();
        }
    }
}
