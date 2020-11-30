package com.lyricslover.onelyrics.pojos;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.lyricslover.onelyrics.misc.Constants;

import org.jsoup.internal.StringUtil;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class AppPreferences {

    private final SharedPreferences sharedPreferences;

    private static AppPreferences appPreferences = null;

    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private Vibrator vibrator;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private LayoutInflater layoutInflater;

    public static AppPreferences getInstance(Context context) {
        if (appPreferences == null) {
            synchronized (AppPreferences.class) {
                appPreferences = new AppPreferences(context);
            }
        }
        return appPreferences;
    }

    public AppPreferences(Context context) {
        sharedPreferences = initSharedPreferences(context);

        this.displayMetrics = getDisplayMetrics();
        this.windowManager = getWindowManager(context);
        this.notificationManager = getNotificationManager(context);
        this.notificationBuilder = getNotificationBuilder(context);
        this.layoutInflater = getLayoutInflater(context);
        this.vibrator = getVibrator(context);

        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }

    public Vibrator getVibrator() {
        return vibrator;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    //public NotificationManager getNotificationManager() { return notificationManager; }
    //public Notification.Builder getNotificationBuilder(){ return  notificationBuilder; }
    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public String getSelectedTheme() {
        return getPreference(Constants.PRODUCT_ID, false)
                ? getPreference(Constants.PREF_APP_THEME, "1")
                : "1";
    }

    //public void setSelectedTheme(String theme){ setPreference(Constants.PREF_APP_THEME, theme); }

    public int getTriggerWidth() {
        return getPreference(Constants.TRIGGER_WIDTH, 0) * 2;
    }

    //public void setTriggerWidth(int triggerWidth) { setPreference(Constants.TRIGGER_WIDTH, triggerWidth); }

    public int getTriggerHeight() {
        return getPreference(Constants.TRIGGER_HEIGHT, 82) * 2;
    }

    //public void setTriggerHeight(int triggerHeight) { setPreference(Constants.TRIGGER_HEIGHT, triggerHeight); }

    public int getTriggerPosition() {
        return getPreference(Constants.TRIGGER_POS, 2);
    }

    //public void setTriggerPosition(int triggerPosition) { setPreference(Constants.TRIGGER_POS, triggerPosition); }

    public int getSwipeDirection() {
        return getPreference(Constants.SWIPE_DIRECTION, 3);
    }

    //public void setSwipeDirection(int swipeDirection) { setPreference(Constants.SWIPE_DIRECTION, swipeDirection); }

    public int getTriggerAlpha() {
        return getPreference(Constants.TRIGGER_ALPHA, 32);
    }

    //public void setTriggerAlpha(int triggerAlpha) { setPreference(Constants.TRIGGER_ALPHA, triggerAlpha); }

    public int getTriggerOffset() {
        return getPreference(Constants.TRIGGER_OFFSET, 52);
    }

    //public void setTriggerOffset(double triggerOffset) {setPreference(Constants.TRIGGER_OFFSET, (float) triggerOffset);}

    public int getPanelHeight() {
        return getPreference(Constants.PANEL_HEIGHT, 26) * displayMetrics.heightPixels / 100;
    }

    //public void setPanelHeight(int panelHeight) { setPreference(Constants.PANEL_HEIGHT, panelHeight); }

    public int getPanelAlpha() {
        return getPreference(Constants.PANEL_ALPHA, 88);
    }

    //public void setPanelAlpha(int panelAlpha) { setPreference(Constants.PANEL_ALPHA, panelAlpha); }

    //public boolean isHidePanelHead() { return  getPreference(Constants.PANEL_HEAD, true); }

    //public void setHidePanelHead(boolean hidePanelHead) { setPreference(Constants.PANEL_HEAD, hidePanelHead); }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public SharedPreferences initSharedPreferences(Context context) {
        return context.getSharedPreferences("com.lyricslover.onelyrics", Context.MODE_PRIVATE);
    }

    public boolean getPreference(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    public int getPreference(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    public float getPreference(String key, float defaultValue) {
        return getSharedPreferences().getFloat(key, defaultValue);
    }

    public long getPreference(String key, long defaultValue) {
        return getSharedPreferences().getLong(key, defaultValue);
    }

    public String getPreference(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    public void setPreference(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).apply();
    }

    public void setPreference(String key, int value) {
        getSharedPreferences().edit().putInt(key, value).apply();
    }

    public void setPreference(String key, long value) {
        getSharedPreferences().edit().putLong(key, value).apply();
    }

    public void setPreference(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    public Song getSongFromPreferences() {
        SharedPreferences pref = getSharedPreferences();
        String artist = pref.getString(Constants.ARTIST, "");
        String track = pref.getString(Constants.TRACK, "");

        Song song = null;
        if (!StringUtil.isBlank(artist) && !StringUtil.isBlank(track)) {
            song = new Song(track, artist, 0, 0);
        }
        return song;
    }

    public void setSongToPreferences(Song song) {
        setPreference(Constants.ARTIST, song.getArtist());
        setPreference(Constants.TRACK, song.getTrack());
    }

    public boolean getBoolean(String value, boolean defaultValue) {
        return getSharedPreferences().getBoolean(value, defaultValue);
    }

    public DisplayMetrics getDisplayMetrics() {
        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
        }
        return displayMetrics;
    }

    private WindowManager getWindowManager(Context context) {
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        }
        return windowManager;
    }

    private Vibrator getVibrator(Context context) {
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        }
        return vibrator;
    }

    private NotificationManager getNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private Notification.Builder getNotificationBuilder(Context context) {
        if (notificationBuilder == null) {
            notificationBuilder = new Notification.Builder(context);
        }
        return notificationBuilder;
    }

    private LayoutInflater getLayoutInflater(Context context) {
        if (layoutInflater == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        return layoutInflater;
    }
}