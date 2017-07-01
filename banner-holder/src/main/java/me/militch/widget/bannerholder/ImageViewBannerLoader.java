package me.militch.widget.bannerholder;

import android.content.Context;
import android.widget.ImageView;

public abstract class ImageViewBannerLoader implements BannerLoader<ImageView>{
    @Override
    public ImageView createView(Context context) {
        return new ImageView(context);
    }
}
