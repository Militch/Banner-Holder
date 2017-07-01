package me.militch.widget.bannerholder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BannerHolderView extends FrameLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private HolderViewPager mViewPager;
    private RadioGroup radioGroup;//指示器
    private List<RadioButton> buttons;//指示器控件集合
    private List<View> views ;//图片展示控件集合
    private int bannerCount;
    private int currentItem;//当前位置
    private Handler handler = new Handler();
    private int switchDuration;//切换时间
    private BannerLoader bannerLoader;//banner加载器
    private boolean isAutoLooper;//是否自动轮播
    private int indicatorResId;//指示器资源ID
    private long looperTime;//轮播时间
    private List<?> banners;
    private BannerClickListener bannerClickListener;
    public BannerHolderView(Context context) {
        this(context,null);
    }

    public BannerHolderView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public BannerHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buttons = new ArrayList<>();
        views = new ArrayList<>();
        initAttributeSet(context,attrs);
        initView();
    }

    private void initAttributeSet(Context context,AttributeSet attr) {
        TypedArray typedArray = context.obtainStyledAttributes(attr, R.styleable.HolderAttr);
        this.indicatorResId = typedArray.getResourceId(R.styleable.HolderAttr_indicatorResId, R.drawable.banner_holder_selector);
        this.switchDuration = typedArray.getInteger(R.styleable.HolderAttr_switchDuration, 800);
        this.looperTime = typedArray.getInteger(R.styleable.HolderAttr_looperTime, 2000);
        this.isAutoLooper = typedArray.getBoolean(R.styleable.HolderAttr_isAutoLooper, false);
    }



    private void initView(){
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.widget_banner_holder,null);
        mViewPager = (HolderViewPager) layout.findViewById(R.id.banner_holder_vp);
        initViewPagerScroll();
        radioGroup = (RadioGroup) layout.findViewById(R.id.banner_selects);
        addView(layout);
        initViewPager();
    }




    /**
     * 设置图片对象集合
     * @param banners 数据集合
     */
    public void setBanners(List<?> banners){
        this.banners = banners;
        bannerCount = banners.size();
    }

    public void setSwitchDuration(int switchDuration) {
        this.switchDuration = switchDuration;
    }

    public void setBannerLoader(BannerLoader bannerLoader) {
        this.bannerLoader = bannerLoader;
    }

    public void setAutoLooper(boolean autoLooper) {
        isAutoLooper = autoLooper;
    }

    public void setIndicatorResId(int indicatorResId) {
        this.indicatorResId = indicatorResId;
    }

    public void setLooperTime(long looperTime) {
        this.looperTime = looperTime;
    }

    public void setBannerClickListener(BannerClickListener bannerClickListener) {
        this.bannerClickListener = bannerClickListener;
    }

    /**
     * 初始化Viewpager滑动切换持续时间
     */
    private void initViewPagerScroll(){
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(mViewPager,bannerScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initViewPager(){
        mViewPager.setFocusable(true);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
    }



    public void start(){
        setImageViews(banners);
    }
    /**
     * 设置ImageView集合
     */
    private void setImageViews(List<?> list){
        if(list==null||list.size()<=0){
            return;
        }
        if(bannerCount==1){
            mViewPager.setScroller(false);
        }else{
            mViewPager.setScroller(true);
        }
        buttons.clear();
        for(int i=0;i<=bannerCount+1;i++){
            Object obj = null;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            View showView = null;
            if(bannerLoader !=null){
                showView = bannerLoader.createView(getContext());
            }
            if(showView == null){
                showView = new ImageView(getContext());
                showView.setLayoutParams(params);
            }
            if(i==0){
                obj = list.get(bannerCount-1);
            }else if(i==bannerCount+1){
                obj = list.get(0);
            }else{
                obj = list.get(i-1);
                addIndex();
            }
            showView.setTag(i);
            showView.setOnClickListener(this);
            views.add(showView);
            if(bannerLoader != null){
                bannerLoader.loadImage(getContext(),obj,showView);
            }
        }
        mPagerAdapter.notifyDataSetChanged();
        updateView();
    }

    /**
     * 添加指示器
     */
    private void addIndex(){
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10,0,10,0);
        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setLayoutParams(layoutParams);
        radioButton.setButtonDrawable(indicatorResId);
        radioButton.setEnabled(false);
        buttons.add(radioButton);
    }
    /**
     * 更新View
     */
    private void updateView(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                radioGroup.removeAllViews();
                mViewPager.setCurrentItem(1,false);
                for(int i=0;i<buttons.size();i++){
                    RadioButton button = buttons.get(i);
                    button.setChecked(i==0?true:false);
                    radioGroup.addView(button);
                }
                if(bannerCount>1&&isAutoLooper){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startAutoLooper();
                        }
                    });
                }
            }
        });
    }

    private void startAutoLooper(){
        handler.removeCallbacks(task);
        handler.postDelayed(task,looperTime);
    }
    private void stopAutoLooper(){
        handler.removeCallbacks(task);
    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if(isAutoLooper){
                currentItem = currentItem%(bannerCount+1)+1;
                if(currentItem == 1){
                    mViewPager.setCurrentItem(currentItem,false);
                    handler.postDelayed(task,looperTime);
                }else if(currentItem == bannerCount+1){
                    mViewPager.setCurrentItem(currentItem,true);
                    handler.postDelayed(task,looperTime);
                }else{
                    mViewPager.setCurrentItem(currentItem,true);
                    handler.postDelayed(task,looperTime);
                }
            }
        }
    };

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return views.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }
    };

    private Scroller bannerScroller = new Scroller(getContext()){
        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, switchDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, switchDuration);
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(positionOffset==0&&position<1){
            position = bannerCount;
            mViewPager.setCurrentItem(position,false);
        }else if(positionOffset==0&&position>bannerCount){
            position = 1;
            mViewPager.setCurrentItem(position,false);
        }
        currentItem = position;
    }

    @Override
    public void onPageSelected(int position) {
        if(position==bannerCount+1){//到最后一页
            checkOutButton(0);
        }else if(position==0){//到起始页
            checkOutButton(buttons.size()-1);
        }else{
            checkOutButton(position-1);
        }
    }
    private void checkOutButton(int p){
        for(int i=0;i<buttons.size();i++){
            buttons.get(i).setChecked(i==p);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        currentItem = mViewPager.getCurrentItem();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(isAutoLooper){
            int action = ev.getAction();
            if(action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE){
                startAutoLooper();
            }else if(action == MotionEvent.ACTION_DOWN){
                stopAutoLooper();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if(bannerClickListener!=null){
            bannerClickListener.onBannerClick( views.indexOf(v)-1);
        }
    }
}
