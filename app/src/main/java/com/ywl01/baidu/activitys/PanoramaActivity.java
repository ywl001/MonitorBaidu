package com.ywl01.baidu.activitys;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.widget.RadioGroup;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.ywl01.baidu.BaseApplication;
import com.ywl01.baidu.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PanoramaActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.panorama_view)
    PanoramaView panoramaView;

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;

    @Override
    protected void initView() {
        initBMapManager();
        setContentView(R.layout.activity_panorama);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        super.initData();
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat",0);
        double lng = intent.getDoubleExtra("lng",0);

       // panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);
        panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
        panoramaView.setPanorama(lng,lat);

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initActionBar() {
        getSupportActionBar().hide();
    }

    private void initBMapManager() {
        BaseApplication app = (BaseApplication) this.getApplication();
        if (app.mapManager == null) {
            app.mapManager = new BMapManager(app);
            app.mapManager.init(new BaseApplication.MyGeneralListener());
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radio_high:
                panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);
                break;
            case R.id.radio_middle:
                panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
                break;
            case R.id.radio_low:
                panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionLow);
                break;

            default:
                panoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
                break;
        }
    }
}
