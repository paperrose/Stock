package com.artfonapps.clientrestore.views.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

/**
 * Created by Emil on 15.08.2016.
 */
public class VerticalViewPager extends ViewPager {
    public enum SwipeDirection {
        all, up, down, none ;
    }

    private float initialYValue;
    private SwipeDirection direction;

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.direction = SwipeDirection.all;
        setPageTransformer(true, new VerticalPageTransformer());
        setOverScrollMode(OVER_SCROLL_NEVER);
        initSpeedViewPager();
    }

    private class VerticalPageTransformer implements PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                view.setAlpha(1);
                view.setTranslationX(view.getWidth() * -position);
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);
            } else {
                view.setAlpha(0);
            }
        }
    }


    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();
        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;
        ev.setLocation(newX, newY);
        return ev;
    }

    private boolean IsSwipeAllowed(MotionEvent event) {
        if(this.direction == SwipeDirection.all) return true;

        if(direction == SwipeDirection.none )//disable any swipe
            return false;

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            initialYValue = event.getY();
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_MOVE) {
            try {
                float diffY = event.getY() - initialYValue;
                if (diffY > 0 && direction == SwipeDirection.up ) {
                    return false;
                } else if (diffY < 0 && direction == SwipeDirection.down ) {
                    // swipe from right to left detected
                    return false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return true;
    }

    private SpeedScroller scroller = null;

    private void initSpeedViewPager() {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            scroller = new SpeedScroller(getContext(),
                    (Interpolator) interpolator.get(null));
            scrollerField.set(this, scroller);
        } catch (Exception e) {
        }
    }

    public void setScrollSpeed(float scrollSpeed) {
        scroller.setScrollSpeed(scrollSpeed);
    }

    public void setAllowedSwipeDirection(SwipeDirection direction) {
        this.direction = direction;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        if (this.IsSwipeAllowed(ev)) {
            boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
            swapXY(ev);
            return intercepted;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.IsSwipeAllowed(ev)) {
            return super.onTouchEvent(swapXY(ev));
        }
        return false;
    }

}