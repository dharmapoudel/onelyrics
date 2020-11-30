package com.lyricslover.onelyrics.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.helpers.LyricsPanelHelper;
import com.lyricslover.onelyrics.helpers.NotificationHelper;
import com.lyricslover.onelyrics.helpers.PermissionHelper;
import com.lyricslover.onelyrics.helpers.TriggerHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.receivers.SystemDialogCloseReceiver;
import com.lyricslover.onelyrics.tasks.AlbumArtDownloaderTask;
import com.lyricslover.onelyrics.tasks.FetchLyricsTask;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class LyricsService extends Service {

    //View trigger;
    //LinearLayout lyricsPanel;
    private AppPreferences appPreferences;


    private TriggerHelper triggerHelper;
    private LyricsPanelHelper panelHelper;
    private NotificationHelper notificationHelper;

    private FetchLyricsTask fetchLyricsTask;
    private AlbumArtDownloaderTask albumArtDownloaderTask;

    private Handler lyricsServiceHandler;
    private Intent lyricsServiceIntent;

    //private boolean restartService;

    //private static final String TAG = LyricsService.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();

        //initialize notification holder
        notificationHelper = (notificationHelper == null) ? new NotificationHelper(getApplicationContext()) : notificationHelper;
        //run service in foreground
        if (Utils.isO()) {
            startForeground(Constants.NOTIFICATION_ID - 1, notificationHelper.notificationBuilder(null, null, null, null).build());
        }

        //initialize app preferences
        appPreferences = new AppPreferences(getApplicationContext());

        //● initialize lyrics panel helper
        panelHelper = (panelHelper == null) ? new LyricsPanelHelper(getApplicationContext()) : panelHelper;
        panelHelper.registerOnSharedPreferenceChangeListener();

        //attach the panel but set the height to zero initially
        //panelHelper.attachLyricsPanel();
        //panelHelper.updateLyricsPanel(0);


        //● initialize lyrics panel helper
        triggerHelper = (triggerHelper == null) ? new TriggerHelper(getApplicationContext()) : triggerHelper;
        triggerHelper.registerOnSharedPreferenceChangeListener();

        lyricsServiceHandler = new LyricsHandler(this); //getHandler(panelHelper, appPreferences, lyricsServiceIntent, getApplicationContext());
        triggerHelper.attachTrigger(lyricsServiceHandler, panelHelper);

        //register lyrics and album art download receiver
        if (PermissionHelper.allPermissionsAvailable(getApplicationContext())) {
            registerDownloadedActionReceiver();
        }

        //un/register home, back, recent button receiver
        if (appPreferences.getPreference(Constants.PREF_CLOSE_ON_BACK_PRESS, false)) {
            registerSystemDialogCloseReceiver();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        lyricsServiceIntent = intent;

        //returns immediately if intent is null
        if (!PermissionHelper.allPermissionsAvailable(getApplicationContext()) || lyricsServiceIntent == null) {
            return Service.START_STICKY_COMPATIBILITY;
        }

        //initialize app variables
        //TO DO: decide on getting isMusicPlaying value from lyricsServiceIntent or app preferences
        Song song = Utils.getSongFromIntent(lyricsServiceIntent);
        //boolean closePanel = lyricsServiceIntent.getBooleanExtra(Constants.CLOSE_LYRICS_PANEL, false);
        boolean isMusicPlaying = appPreferences.getPreference(Constants.IS_MUSIC_PLAYING, false); //intent.getBooleanExtra(Constants.IS_MUSIC_PLAYING, false);
        boolean autoHideTrigger = appPreferences.getPreference(Constants.AUTO_HIDE_TRIGGER, false);
        //boolean autoClosePanel = appPreferences.getPreference(Constants.PREF_AUTO_CLOSE_PANEL, false);
        //boolean isReadingOffline = intent.getBooleanExtra(Constants.OFFLINE_READ, false);
        // boolean shortcutActionShowLyricsPanel = lyricsServiceIntent.getBooleanExtra(Constants.SHORTCUT_LYRICS_PANEL, false);
        boolean trackChanged = intent.getBooleanExtra(Constants.TRACK_CHANGED, false);

        //boolean openPanel = lyricsServiceIntent.getBooleanExtra(Constants.OPEN_LYRICS_PANEL, false);
        //boolean isTrackChanged = lyricsServiceIntent.getBooleanExtra(Constants.TRACK_CHANGED, false);

        //● restart service
        //if ( Constants.PREF_CLOSE_ON_BACK_PRESS.equalsIgnoreCase(intent.getAction())){
        //stopService(new Intent(this, LyricsService.class));
        //  stopSelf();
        //}

        //● song is not null save it to preferences
        if (song != null) {
            appPreferences.setSongToPreferences(song);
        }

        //● fetch lyrics
        //if (song != null && (isMusicPlaying || isReadingOffline)) {
        fetchLyrics(lyricsServiceIntent, song);
        //}

        //● auto hide trigger if music is not playing
        if (!isMusicPlaying && autoHideTrigger) {
            triggerHelper.updateTrigger(0);
        }

        //● reset the lyrics panel if track is changed ◯
        if (trackChanged) {
            panelHelper.resetLyricsPanel(getApplicationContext(), song);
        }

        //● clear notification
        //if (!closePanel && !shortcutActionShowLyricsPanel) {
        //hide the notification by default
        //notificationHelper.sendNotification(lyricsServiceIntent, lyricsServiceHandler);

        //}

        //remove the notification when music is not playing or not reading offline
        //if (!isMusicPlaying && !isReadingOffline) {
        //    notificationHelper.cancelNotification(Constants.NOTIFICATION_ID);
        //}

        return Service.START_STICKY_COMPATIBILITY;
    }

    private BroadcastReceiver dataChangeReceiver = new BroadcastReceiver() {

        //lyrics and album art is downloaded
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                //get song from intent
                Song song = Utils.getSongFromIntent(intent);

                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LYRICS_DOWNLOADED)) {
                    boolean isLyricsDownloaded = intent.getBooleanExtra(Constants.ACTION_LYRICS_DOWNLOADED, false);
                    boolean openPanel = intent.getBooleanExtra(Constants.OPEN_LYRICS_PANEL, false);
                    boolean trackChanged = intent.getBooleanExtra(Constants.TRACK_CHANGED, false);
                    boolean closePanel = intent.getBooleanExtra(Constants.CLOSE_LYRICS_PANEL, false);
                    boolean isMusicPlaying = intent.getBooleanExtra(Constants.IS_MUSIC_PLAYING, false);
                    boolean playStateChanged = intent.getBooleanExtra(Constants.PLAY_STATE_CHANGED, false);
                    boolean autoPanel = appPreferences.getPreference(Constants.PREF_AUTO_CLOSE_PANEL, false);

                    //● clear notification if music is not playing
                     if(playStateChanged) notificationHelper.cancelNotification(Constants.NOTIFICATION_ID);

                    //● send notification
                    if ((isMusicPlaying || trackChanged) && isLyricsDownloaded ) {
                        notificationHelper.sendNotification(intent, lyricsServiceHandler);
                    }

                    //● fetch album art
                    if (isLyricsDownloaded) fetchAlbumArt(context, song, intent);

                    //● close lyrics panel if closePanel flag is set
                    if (closePanel || ( autoPanel && !isMusicPlaying && playStateChanged) ) {
                        panelHelper.closeLyricsPanel();
                    }

                    //● show lyrics panel
                    if (openPanel || ( autoPanel && isMusicPlaying && playStateChanged)) {
                        panelHelper.attachAndShowLyricsPanel();
                    }

                    //● update the lyrics shown on lyrics panel
                    if(isLyricsDownloaded) panelHelper.updateLyrics(song, intent);
                }

                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_ALBUMART_DOWNLOADED)) {
                    boolean isAlbumArtDownloaded = intent.getBooleanExtra(Constants.ACTION_ALBUMART_DOWNLOADED, false);

                    //● blur panel
                    if (isAlbumArtDownloaded) panelHelper.blurLyricsPanel(song);
                }
            }
        }

        private void fetchAlbumArt(Context context, Song song, Intent intent) {
            boolean cacheAlbumArt = appPreferences.getPreference(Constants.CACHE_ALBUMART, true);
            if (cacheAlbumArt) {
                if (albumArtDownloaderTask != null) {
                    albumArtDownloaderTask.cancel(true);
                    albumArtDownloaderTask = null;
                }
                albumArtDownloaderTask = new AlbumArtDownloaderTask(intent, context);
                albumArtDownloaderTask.execute(song);
            }
        }
    };

    private void fetchLyrics(Intent intent, Song song) {
        //fetch lyrics
        if (fetchLyricsTask != null && fetchLyricsTask.getStatus() != AsyncTask.Status.FINISHED) {
            fetchLyricsTask.cancel(true);
            fetchLyricsTask = null;
        }

        if (song != null) {
            fetchLyricsTask = new FetchLyricsTask(intent, getApplicationContext());
            fetchLyricsTask.execute(song);
        }
    }

    static class LyricsHandler extends Handler {
        private final WeakReference<LyricsService> mService;

        LyricsHandler(LyricsService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LyricsService service = mService.get();
            if (service != null) {
                try {
                    //show the lyrics panel
                    service.panelHelper.attachAndShowLyricsPanel();

                    // update the lyrics shown on lyrics panel
                    Song song = msg.getData().getParcelable("song");
                    service.panelHelper.updateLyrics(song, service.lyricsServiceIntent);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // remove the lyrics panel and the trigger and unregister the receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
            LinearLayout lyricsPanel = panelHelper.getLyricsPanel();
            final View bottomLayout = lyricsPanel.getChildAt(0);
            bottomLayout.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    lyricsPanel.removeView(bottomLayout);
                    appPreferences.getWindowManager().removeView(lyricsPanel);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            //do not remove trigger just hide
            triggerHelper.updateTrigger(0);
            //windowManager.removeView(trigger);
            panelHelper.updateLyricsPanel(0);

        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }

        //unregister home, back, recent button receiver
        //unregisterAllReceivers();

        //clear notification
        notificationHelper.cancelNotification(Constants.NOTIFICATION_ID);

    }


    private void registerDownloadedActionReceiver() {
        IntentFilter iF = new IntentFilter();
        iF.addAction(Constants.ACTION_LYRICS_DOWNLOADED);
        iF.addAction(Constants.ACTION_ALBUMART_DOWNLOADED);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangeReceiver, iF);
    }

    private void registerSystemDialogCloseReceiver() {
        SystemDialogCloseReceiver systemDialogCloseReceiver = new SystemDialogCloseReceiver();
        IntentFilter mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(systemDialogCloseReceiver, mFilter);
    }

    /*public void unregisterAllReceivers() {
        if (systemDialogCloseReceiver != null) {
            unregisterReceiver(systemDialogCloseReceiver);
        }
        if (dataChangeReceiver != null) {
            unregisterReceiver(dataChangeReceiver);
        }
    }*/

}