package com.lyricslover.onelyrics.receivers;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.AppPreferences;

public class OnSwipeTouchListener implements OnTouchListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private float mDownX;
    private float mDownY;
    private VelocityTracker mVelocityTracker;
    private View mView;
    private WindowManager.LayoutParams mWindowsParams;
    private WindowManager mWindowManager;
    private long startTime = System.currentTimeMillis();

    private int initialY;
    private float initialTouchY;
    private AppPreferences appPreferences;

    public OnSwipeTouchListener(View view, WindowManager.LayoutParams lyricsPanelParams, WindowManager windowManager, AppPreferences appPreferences) {
        this.mView = view;
        this.mWindowsParams = lyricsPanelParams;
        this.mWindowManager = windowManager;
        this.appPreferences = appPreferences;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (System.currentTimeMillis() - startTime <= 300) {
            return false;
        }


        //on touch
        onTouch();

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                initialY = mWindowsParams.y;
                initialTouchY = event.getRawY();


                mDownX = event.getRawX();
                mDownY = event.getRawY();
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(event);

                return true;
            }


            case MotionEvent.ACTION_UP: {
                float diffY = event.getRawY() - mDownY;
                float diffX = event.getRawX() - mDownX;
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityY = mVelocityTracker.getYVelocity();
                float velocityX = mVelocityTracker.getXVelocity();


                boolean result = false;
                try {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;

            }

            case MotionEvent.ACTION_MOVE: {

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);

                int panelY = initialY - ((int) (event.getRawY() - initialTouchY));
                mWindowsParams.y = panelY;
                mWindowManager.updateViewLayout(mView, mWindowsParams);
                appPreferences.setPreference( Constants.PANEL_Y, panelY);
                return true;
            }

        }
        // Further touch is not handled
        return false;
    }


    public void onSwipeRight() {
        //do nothing
    }

    public void onSwipeLeft() {
        //do nothing
    }

    public void onSwipeTop() {
        //do nothing
    }

    public void onSwipeBottom() {
        //do nothing
    }

    public void onTouch() {
        //do nothing
    }

}