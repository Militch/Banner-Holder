package me.militch.widget.bannerholder;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HolderViewPager extends ViewPager {

    private boolean isScroller = true;
    public HolderViewPager(Context context) {
        super(context);
    }

    public HolderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScroller&&super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return super.onInterceptHoverEvent(event);
    }

    public void setScroller(boolean scroller) {
        isScroller = scroller;
    }

}