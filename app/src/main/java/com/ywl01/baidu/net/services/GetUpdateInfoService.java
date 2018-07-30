package com.ywl01.baidu.net.services;

import com.ywl01.baidu.consts.Urls;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by ywl01 on 2018/2/23.
 */

public interface GetUpdateInfoService {
    @GET(Urls.APP_INFO_URL)
    Observable<String> getAppInfo();
}
