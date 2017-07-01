package me.militch.bannerholder;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import me.militch.widget.bannerholder.BannerClickListener;
import me.militch.widget.bannerholder.BannerHolderView;
import me.militch.widget.bannerholder.HolderAttr;
import me.militch.widget.bannerholder.ImageViewBannerLoader;
import me.militch.widget.bannerholder.loader.SimpleImageLoader;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BannerHolderView bannerHolderView = (BannerHolderView) findViewById(R.id.banner_holder);
        List<String> integers = new ArrayList<>();
        integers.add("[URL链接]");
        integers.add("[URL链接]");
        integers.add("[URL链接]");
        integers.add("[URL链接]");
        integers.add("[URL链接]");
        HolderAttr.builder()
                //加载资源集合
                .setBanners(integers)
                //是否自动轮播
                .setAutoLooper(true)
                //设置轮播时间（单位：毫秒）
                .setLooperTime(3000)
                //设置切换时长（单位：毫秒）
                .setSwitchDuration(800)
                //设置Banner点击事件
                .setBannerClickListener(new BannerClickListener() {
                    @Override
                    public void onBannerClick(int p) {
                        //点击位置索引 0~
                    }
                })
                //设置Banner图片加载器可以使用图片加载框架
                .setBannerLoader(new ImageViewBannerLoader() {
                    @Override
                    public void loadImage(Context context, Object path, ImageView view) {
                        /* 提供参数与控件，自定加载图片的实现方式
                         * -选择使用默认提供的ImageLoader加载图片
                         */
                        SimpleImageLoader.getInstence().loadImage((String) path,view);
                    }

                    @Override
                    public ImageView createView(Context context) {
                        ImageView imageView = new ImageView(context);
                        imageView.setBackgroundResource(R.drawable.defult);
                        return imageView;
                    }
                }).builder(bannerHolderView).start();
    }
}
