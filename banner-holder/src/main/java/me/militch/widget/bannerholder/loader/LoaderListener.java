package me.militch.widget.bannerholder.loader;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface LoaderListener {
    void loadSuccess(Bitmap bitmap,ImageView imageView);
    void loadFail(ImageView imageView);
}
