package com.ywl01.baidu.net.services;

import com.ywl01.baidu.consts.Urls;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by ywl01 on 2017/1/30.
 */

public interface FileUploadService {
    @Multipart
    @POST(Urls.UPLOAD_URL)
    Observable<String> upload(@Part("fileDir") RequestBody fileDir, @Part MultipartBody.Part file);
}
