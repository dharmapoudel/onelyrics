
package com.lyricslover.onelyrics.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.AppPreferences;

public class TriggerHelper {

    private final Context context;
    private Vibrator vibrator;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;

    protected static final String TAG = TriggerHelper.class.getSimpleName();
    private static int layoutFlag = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_PHONE : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

    private AppPreferences pref;
    private View trigger;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    public TriggerHelper(Context s) {
        this.context = s;

        this.pref = AppPreferences.getInstance(s.getApplicationContext());
        this.windowManager = pref.getWindowManager();
        this.displayMetrics = pref.getDisplayMetrics();
        this.vibrator = pref.getVibrator();
        //PreferenceManager.getDefaultSharedPreferences(service);
        this.trigger = getTriggerView();
    }

    public void attachTrigger(Handler handler, LyricsPanelHelper panelHelper) {
        removeTrigger();
        windowManager.addView(trigger, getTriggerLayoutParams());
        attachTriggerTouchListeners(trigger, handler, pref, panelHelper);
    }

    public void updateTrigger(int width) {
        WindowManager.LayoutParams triggerParam = getTriggerLayoutParams();
        triggerParam.width = width;
        windowManager.updateViewLayout(trigger, triggerParam);
    }


    public void removeTrigger() {
        try {
            if (trigger != null && trigger.getParent() != null) {
                windowManager.removeView(trigger);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Exception occured while removing trigger" + e.getLocalizedMessage());
        }

    }


    public View getTriggerView() {
        View trig = new View(context);
        trig.setBackgroundResource(R.color.colorPrimaryDarkSemiTransparent);
        trig.getBackground().setAlpha((int) (pref.getTriggerAlpha() * 2.55));
        return trig;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void attachTriggerTouchListeners(final View trigger, final Handler handler, final AppPreferences appPreferences, LyricsPanelHelper panelHelper) {

        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //hide trigger temporarily on single click
                trigger.setVisibility(View.GONE);
                handler.postDelayed(() -> trigger.setVisibility(View.VISIBLE), 3000);
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            /*@Override
            public void onLongPress(MotionEvent e) {
            }*/

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //show  lyrics window on swipe
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    int xDirection = (diffX > 0) ? 4 : 3;
                    int yDirection = (diffY > 0) ? 2 : 1;
                    int direction = (Math.abs(diffX) > Math.abs(diffY)) ? xDirection : yDirection;
                    if (appPreferences.getSwipeDirection() == direction) {
                        vibrate();
                        panelHelper.attachAndShowLyricsPanel();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return false;
            }


        });

        trigger.setOnTouchListener((View v, MotionEvent event) -> gestureDetector.onTouchEvent(event));

    }

    public WindowManager.LayoutParams getTriggerLayoutParams() {
        // params for the trigger window
        WindowManager.LayoutParams triggerParams = new WindowManager.LayoutParams(
                pref.getTriggerWidth(), pref.getTriggerHeight(),
                layoutFlag,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        triggerParams.gravity = Gravity.TOP | ((pref.getTriggerPosition() == 1) ? Gravity.START : Gravity.END);
        triggerParams.x = 0;
        triggerParams.y = displayMetrics.heightPixels - (displayMetrics.heightPixels * pref.getTriggerOffset() / 100);

        return triggerParams;
    }

    private void vibrate() {
        boolean vibrate = pref.getBoolean(Constants.TRIGGER_VIBRATION, false);
        if (vibrate) vibrator.vibrate(25);
    }


    public void registerOnSharedPreferenceChangeListener() {

        sharedPreferenceChangeListener = (sharedPrefs, key) -> {
            //ignore the following key changes
            //TO DO: add option to hide trigger when song is not playing
            /*if(!Constants.TRACK.equalsIgnoreCase(key)
                    && !Constants.ACTION_LYRICS_DOWNLOADED.equalsIgnoreCase(key)
                    && !Constants.ARTIST.equalsIgnoreCase(key)
                    && !Constants.IS_MUSIC_PLAYING.equalsIgnoreCase(key)
                    //&& !Constants.PREF_APP_START.equalsIgnoreCase(key)
                    && !Constants.PREF_APP_THEME.equalsIgnoreCase(key) ) {

                //reset trigger handle width to original width for 3 seconds then hide if music is not playing
                updateTrigger(pref.getTriggerWidth());

                Runnable runnable = () -> {
                        boolean isLyricsAvailableOffline = pref.getPreference(Constants.ACTION_LYRICS_DOWNLOADED, false);
                        if (!isLyricsAvailableOffline) {
                            updateTrigger(0);
                        }
                    };

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(runnable, 2000);
            }*/


            switch (key) {
                case Constants.TRIGGER_OFFSET:
                case Constants.TRIGGER_HEIGHT:
                case Constants.TRIGGER_WIDTH:
                case Constants.TRIGGER_POS:
                    windowManager.updateViewLayout(trigger, getTriggerLayoutParams());
                    break;

                case Constants.TRIGGER_ALPHA:
                    trigger.getBackground().setAlpha((int) (pref.getTriggerAlpha() * 2.55));
                    break;

                default:
                    //do nothing
                    break;
            }
        };

        pref.getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }


}