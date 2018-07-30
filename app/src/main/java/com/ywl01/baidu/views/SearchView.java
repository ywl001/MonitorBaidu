package com.ywl01.baidu.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ywl01.baidu.R;
import com.ywl01.baidu.beans.CameraBean;
import com.ywl01.baidu.consts.SqlAction;
import com.ywl01.baidu.events.CamerasEvent;
import com.ywl01.baidu.events.TypeEvent;
import com.ywl01.baidu.net.HttpMethods;
import com.ywl01.baidu.net.SqlFactory;
import com.ywl01.baidu.net.observers.BaseObserver;
import com.ywl01.baidu.net.observers.CamerasObserver;
import com.ywl01.baidu.utils.AppUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

import static com.ywl01.baidu.R.id.et_search;

/**
 * Created by ywl01 on 2017/3/15.
 */
public class SearchView extends FrameLayout implements TextWatcher,TextView.OnEditorActionListener {

    private Context context;

    @Bind(et_search)
    EditText etSearch;

    @Bind(R.id.btn_clear)
    Button btnClear;

    public SearchView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true);
        ButterKnife.bind(this);
        etSearch.setOnEditorActionListener(this);
        etSearch.addTextChangedListener(this);
    }

    @OnClick(R.id.btn_clear)
    public void onClear() {
        String inputText = etSearch.getText().toString().trim();
        if (inputText.length() > 0) {
            etSearch.setText("");
        }else{
            setVisibility(GONE);
            TypeEvent.send(TypeEvent.SHOW_BTN_CONTAINER);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
//        String input = etSearch.getText().toString().trim();
//        if (input.isEmpty()) {
//            btnClear.setVisibility(GONE);
//        } else {
//            btnClear.setVisibility(VISIBLE);
//        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if("".equals(etSearch.getText().toString().trim())){
                AppUtils.showToast("请输入查询内容。。。");
                return false;
            }

            CamerasObserver camerasObserver = new CamerasObserver();
            String keyword = etSearch.getText().toString().trim();
//            String[] keywords = keyword.split(" ");
            String sql = SqlFactory.selectMarkerBySearch(keyword);
            HttpMethods.getInstance().getSqlResult(camerasObserver, SqlAction.SELECT,sql);
            textView.setText("");
            camerasObserver.setOnNextListener(new BaseObserver.OnNextListener() {
                @Override
                public void onNext(Object data, Observer observer) {
                    List<CameraBean> markers = (List<CameraBean>) data;
                    CamerasEvent event = new CamerasEvent();
                    event.cameraBeans = markers;
                    event.dispatch();
                }
            });
            return true;
        }
        return false;
    }
}
