package me.militch.widget.bannerholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public interface  BannerLoader<V extends View> {
    void loadImage(Context context,Object path,V view);
    V createView(Context context);
}
