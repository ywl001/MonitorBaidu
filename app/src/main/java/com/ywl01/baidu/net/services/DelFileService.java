package com.ywl01.baidu.net.services;

import com.ywl01.baidu.consts.Urls;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by ywl01 on 2017/1/30.
 */

public interface DelFileService {
    @FormUrlEncoded
    @POST(Urls.DEL_URL)
    Observable<String> delFile(@Field("files") String filePaths);
}
