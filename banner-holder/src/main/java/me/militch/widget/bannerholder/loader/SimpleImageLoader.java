package me.militch.widget.bannerholder.loader;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SimpleImageLoader {
    private static SimpleImageLoader mInstence;
    private LruCache<String,Bitmap> mLruCache;//图片缓存对象
    private ExecutorService mThreadPool;//线程池
    private static final int DEFULE_THREAD_COUNT = 4;//默认线程数量
    private LinkedList<Runnable> taskQueue;//任务队列
    private Thread mThread;//后台轮询线程
    private Handler mPoolThreadHandler;//
    private Handler mUiHandler;//UI线程Handler
    private Semaphore semaphore = new Semaphore(0);//初始化信号量
    private Semaphore taskSemaphore;//任务队列信号量
    private LoaderListener loaderListener;
    private SimpleImageLoader(int threadCount){
        init(threadCount);
    }

    public SimpleImageLoader setLoaderListener(LoaderListener loaderListener) {
        this.loaderListener = loaderListener;
        return this;
    }

    /**
     * 初始化
     * @param threadCount 线程数量
     */
    private void init(int threadCount) {
        mThread = new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(taskQueue.removeFirst());
                        try {
                            taskSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                //释放信号量
                semaphore.release();
                Looper.loop();
            }
        };
        mThread.start();
        mLruCache = new LruCache<String,Bitmap>(getCacheMenmory()){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        taskQueue = new LinkedList<>();
        taskSemaphore = new Semaphore(threadCount);

    }

    /**
     * 获取缓存大小
     * @return
     */
    private int getCacheMenmory(){
        int maxMenmory = (int) Runtime.getRuntime().maxMemory();
        return maxMenmory/8;
    }

    public static SimpleImageLoader getInstence(){
        if(mInstence == null){
            synchronized (SimpleImageLoader.class){
                if(mInstence == null){
                    mInstence = new SimpleImageLoader(DEFULE_THREAD_COUNT);
                }
            }
        }
        return mInstence;
    }

    /**
     * 加载图片
     * @param path 图片路径
     * @param view ImageView
     */
    public void loadImage(String path, ImageView view){
        view.setTag(path);
        if(mUiHandler == null){
            mUiHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //设置图片
                    ImageHolder imageHolder = (ImageHolder) msg.obj;
                    Bitmap bitmap = imageHolder.bitmap;
                    String path = imageHolder.path;
                    ImageView imageView = imageHolder.imgView;
                    if(imageView.getTag().toString().equals(path)){
                        if(loaderListener!=null){
                            loaderListener.loadSuccess(bitmap,imageView);
                        }else{
                            imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
                        }
                    }
                }
            };
        }
        Bitmap bm = getBitmap4Cache(path);
        if(bm != null){
            refreashBitmap(bm,view,path);
        }else{
            addTask(new DownloadImage(path,view));
        }
    }

    /**
     * 添加任务
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        taskQueue.add(runnable);
        try {
            if(mPoolThreadHandler == null){
                semaphore.acquire();
            }
        } catch (InterruptedException e) {

        }
        mPoolThreadHandler.sendEmptyMessage(0x11);
    }

    /**
     * 从缓存中获取Bitmap
     * @param path
     * @return
     */
    private Bitmap getBitmap4Cache(String path) {
        return mLruCache.get(path);
    }

    /**
     * 添加Bitmap到缓存
     * @param path
     * @param bitmap
     */
    private void addBitmap2Cache(String path, Bitmap bitmap){
        if(getBitmap4Cache(path) == null){
            if(bitmap != null){
                mLruCache.put(path,bitmap);
            }
        }
    }
    private void refreashBitmap(Bitmap bm, ImageView imageView, String path){
        Message msg = Message.obtain();
        ImageHolder holder = new ImageHolder();

        holder.bitmap = bm;
        holder.imgView = imageView;
        holder.path = path;

        msg.obj = holder;
        mUiHandler.sendMessage(msg);
    }
    private class ImageHolder{
         Bitmap bitmap;
         ImageView imgView;
         String path;
    }


    private class DownloadImage implements Runnable{//, HttpUtil.OnInputStreamListener{
        private String path;
        private ImageView view;
        public DownloadImage(String path,ImageView view){
            this.path = path;
            this.view = view;
        }
        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                URL url = new URL(path);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                int code = urlConnection.getResponseCode();
                if(HttpURLConnection.HTTP_OK == code){
                    inputStream = urlConnection.getInputStream();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    addBitmap2Cache(path,bm);
                    refreashBitmap(bm, view, path);
                    taskSemaphore.release();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(loaderListener!=null){
                    loaderListener.loadFail(view);
                }

            }finally {
                try {
                    if(inputStream!=null) inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

