package me.militch.widget.bannerholder;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HolderAttr {
    private BannerHolderView bannerHolderView;
    private HolderAttr(BannerHolderView bannerHolderView) {
        this.bannerHolderView = bannerHolderView;
    }
    public static Builder builder(){
        return new Builder();
    }
    public void start(){
        bannerHolderView.start();
    }
    public static class Builder{
        private List banners = new ArrayList<>();
        private Object[] objs;
        private int switchDuration = 800;//切换时间
        private BannerLoader bannerLoader;//banner加载器
        private boolean isAutoLooper = false;//是否自动轮播
        private int indicatorResId = R.drawable.banner_holder_selector;//指示器资源ID
        private long looperTime = 1000;//轮播时间
        private BannerClickListener bannerClickListener;
        private Builder(){}
        public Builder setBanners(List<?> banners) {
            this.banners = banners;
            return this;
        }
        public Builder setSwitchDuration(int switchDuration) {
            this.switchDuration = switchDuration;
            return this;
        }

        public Builder setBannerLoader(BannerLoader bannerLoader) {
            this.bannerLoader = bannerLoader;
            return this;
        }

        public Builder setAutoLooper(boolean autoLooper) {
            isAutoLooper = autoLooper;
            return this;
        }

        public Builder setIndicatorResId(int indicatorResId) {
            this.indicatorResId = indicatorResId;
            return this;
        }

        public Builder setLooperTime(long looperTime) {
            this.looperTime = looperTime;
            return this;
        }

        public Builder setBannerClickListener(BannerClickListener bannerClickListener) {
            this.bannerClickListener = bannerClickListener;
            return this;
        }

        public HolderAttr builder(BannerHolderView bannerHolderView){
            bannerHolderView.setLooperTime(looperTime);
            bannerHolderView.setIndicatorResId(indicatorResId);
            bannerHolderView.setAutoLooper(isAutoLooper);
            bannerHolderView.setSwitchDuration(switchDuration);
            bannerHolderView.setBanners(banners);
            bannerHolderView.setBannerLoader(bannerLoader);
            bannerHolderView.setBannerClickListener(bannerClickListener);
            return new HolderAttr(bannerHolderView);
        }
    }
}
