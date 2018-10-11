package com.ywl01.baidu.views;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ywl01.baidu.R;
import com.ywl01.baidu.activitys.BaseActivity;
import com.ywl01.baidu.utils.AppUtils;
import com.ywl01.baidu.views.holders.BaseHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ywl01 on 2018/2/27.
 */

public class ImagePageViewHolder extends BaseHolder<String> implements OnPhotoTapListener{
    @BindView(R.id.photo_view)
    PhotoView photoView;

    @BindView(R.id.loading)
    ProgressBar progressBar;

    @Override
    protected View initView() {
        View view = View.inflate(AppUtils.getContext(), R.layout.image_detail, null);
        ButterKnife.bind(this, view);
        photoView.setOnPhotoTapListener(this);
        return view;
    }

    @Override
    protected void refreshUI(String data) {
        ImageLoader.getInstance().displayImage(data,photoView,new ImageLoadingListener(progressBar));
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        BaseActivity.currentActivity.finish();
    }

    class ImageLoadingListener extends SimpleImageLoadingListener {
        private ProgressBar progressBar;
        public ImageLoadingListener(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            String message = null;
            switch (failReason.getType()) {
                case IO_ERROR:
                    message = "下载错误";
                    break;
                case DECODING_ERROR:
                    message = "图片无法显示";
                    break;
                case NETWORK_DENIED:
                    message = "网络有问题，无法下载";
                    break;
                case OUT_OF_MEMORY:
                    message = "图片太大无法显示";
                    break;
                case UNKNOWN:
                    message = "未知的错误";
                    break;
            }
            AppUtils.showToast(message);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
