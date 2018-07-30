package com.ywl01.baidu.net.observers;

/**
 * Created by ywl01 on 2017/3/17.
 */

public class LoginObserver extends BaseObserver<String> {
    @Override
    protected String transform(String data) {
        return data;
    }
}
