package com.lyricslover.onelyrics.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import com.lyricslover.onelyrics.helpers.NotificationHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.services.LyricsService;

import java.util.List;


public class MediaListener extends NotificationListenerService {

    private MediaSessionManager mediaSessionManager;
    private MediaController mediaController;
    public static final String MEDIA_ACTION = Constants.MEDIA_ACTION;
    private ComponentName componentName;
    long duration;
    long position;
    private AppPreferences appPreferences;
    //private LocalAlbumArtDownloaderTask localAlbumArtDownloaderTask;

    protected static final String TAG = MediaListener.class.getSimpleName();

    @Override
    public void onCreate() {

        //initialize app preferences
        appPreferences = new AppPreferences(getApplicationContext());

        if (Utils.isO()) {
            startForeground(Constants.NOTIFICATION_ID - 1, (new NotificationHelper(getApplicationContext())).notificationBuilder(null, null, null, null).build());
        }

        registerReceiver(broadcastReceiver, new IntentFilter(MEDIA_ACTION));

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        componentName = new ComponentName(getApplicationContext(), MediaListener.class);

        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName);
            List<MediaController> controllers = mediaSessionManager.getActiveSessions(componentName);
            mediaController = pickController(controllers);
            if (mediaController != null) {
                mediaController.registerCallback(callback);
                meta = mediaController.getMetadata();
                updateMetadata();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception occured onCreate");
        }
    }

    @Override
    public int onStartCommand(Intent i, int startId, int i2) {

        if (i == null) {
            return Service.START_STICKY;
        }

        /*if (Constants.PREF_APP_STOP.equalsIgnoreCase(i.getAction())) {
            if (Utils.isO()) stopForeground(true);
            stopSelf();
        }*/

        if (mediaController == null) {
            try {
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(componentName);
                mediaController = pickController(controllers);
                if (mediaController != null) {
                    mediaController.registerCallback(callback);
                    meta = mediaController.getMetadata();
                    updateMetadata();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occured onStartCommand");
            }
        }
        return START_STICKY;
    }

    MediaController.Callback callback = new MediaController.Callback() {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            mediaController = null;
            meta = null;
        }

        /*@Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
            //updateMetadata();
        }*/

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            //currentlyPlaying = state.getStat                e() == PlaybackState.STATE_PLAYING;
            //boolean playBackStateChanged = false;
            updateMetadata();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            meta = metadata;
            //boolean metaDataChanged = true;
            updateMetadata();
        }

        /*@Override
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
        }

        @Override
        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }*/
    };

    /*@Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) { }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) { }*/

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        mediaController = null;
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener);
    }

    MediaSessionManager.OnActiveSessionsChangedListener sessionListener = (List<MediaController> controllers) -> {
        if (controllers != null) {
            mediaController = pickController(controllers);
            if (mediaController == null) return;
            mediaController.registerCallback(callback);
            meta = mediaController.getMetadata();

            currentlyPlaying = mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
            //currentlyPlaying =  (mediaController.getPlaybackState() != null) ? mediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING: false;
            //boolean activeSessionChanged = true;
            updateMetadata();
        }
    };

    private boolean currentlyPlaying = false;
    private String currentArtist;
    private String currentTrack;

    public void updateMetadata() {
        if (mediaController != null && mediaController.getPlaybackState() != null) {
            currentlyPlaying = mediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
            position = mediaController.getPlaybackState().getPosition();
        }
        if (meta == null) return;

        currentArtist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST);
        currentTrack = meta.getString(MediaMetadata.METADATA_KEY_TITLE);
        if (currentTrack == null) {
            currentTrack = meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        }
        //String currentAlbum = meta.getString(MediaMetadata.METADATA_KEY_ALBUM);

        if (currentArtist == null) currentArtist = "";
        if (currentTrack == null) currentTrack = "";
        /*if (currentAlbum == null) currentAlbum = "";

        Bitmap currentArt = meta.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (currentArt == null) {
            currentArt = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        }*/
        duration = meta.getLong(MediaMetadata.METADATA_KEY_DURATION);


        sendBroadcast(new Intent(MEDIA_ACTION));
    }

    private MediaController pickController(List<MediaController> controllers) {
        for (int i = 0; i < controllers.size(); i++) {
            MediaController mc = controllers.get(i);
            if (mc != null && mc.getPlaybackState() != null &&
                    mc.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                return mc;
            }
        }
        if (!controllers.isEmpty()) return controllers.get(0);
        return null;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private MediaMetadata meta;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //send an intent to LyricsService
            Intent intent1 = new Intent(context, LyricsService.class);
            intent1.putExtra(Constants.ARTIST, currentArtist);
            intent1.putExtra(Constants.TRACK, currentTrack);
            intent1.putExtra(Constants.POSITION, position);
            intent1.putExtra(Constants.DURATION, duration);

            //fetch  last song info
            String lastArtist = appPreferences.getPreference(Constants.ARTIST, "");
            String lastTrack = appPreferences.getPreference(Constants.TRACK, "");
            boolean lastPlayingState = appPreferences.getPreference(Constants.IS_MUSIC_PLAYING, false);
            boolean isTrackChanged = isTrackChanged(currentArtist, lastArtist, currentTrack, lastTrack);
            long lastPosition = appPreferences.getPreference(Constants.MUSIC_POSITION, 0L);
            boolean isPositionChanged = isPositionChanged(position, lastPosition);
            boolean isPlayStateChanged = isPlayStateChanged(currentlyPlaying, lastPlayingState);
            boolean appExcluded = appPreferences.getPreference(Constants.PRODUCT_ID, false) && checkIfAppExcluded(mediaController);

            //set song changed flag and save song
            intent1.putExtra(Constants.TRACK_CHANGED, isTrackChanged);
            if (isTrackChanged) {
                appPreferences.setPreference(Constants.ARTIST, currentArtist);
                appPreferences.setPreference(Constants.TRACK, currentTrack);
            }

            //if (isPlayStateChanged) {
            intent1.putExtra(Constants.IS_MUSIC_PLAYING, currentlyPlaying);
            intent1.putExtra(Constants.PLAY_STATE_CHANGED, isPlayStateChanged);
            appPreferences.setPreference(Constants.IS_MUSIC_PLAYING, currentlyPlaying);
            //appPreferences.setPreference(Constants.PLAY_STATE_CHANGED, isPlayStateChanged);
            //}

            if(isPositionChanged){
                appPreferences.setPreference(Constants.MUSIC_POSITION, position);
            }

            //add the current time to intent
            intent1.putExtra(Constants.TIME_ELAPSED, System.currentTimeMillis());


            if ((isTrackChanged || isPlayStateChanged || isPositionChanged) && !appExcluded) {
                context.startService(intent1);
            }
        }

        private boolean checkIfAppExcluded(MediaController mediaController) {
            boolean isAppExcluded = (mediaController == null);
            if (mediaController != null) {
                String appName = Utils.getAppNameFromPackage(getPackageManager(), mediaController.getPackageName());
                String excludedApps = appPreferences.getPreference(Constants.PREF_EXCLUDE_APPS, "");
                isAppExcluded = Utils.getSelectedAppsList(excludedApps).contains(appName);
            }
            return isAppExcluded;
        }

        private boolean isPositionChanged(long currentPosition, long lastPosition) {
            return currentPosition != lastPosition;
        }

        private boolean isTrackChanged(String currentArtist, String lastArtist, String currentTrack, String lastTrack) {
            return !(currentArtist.equalsIgnoreCase(lastArtist) && currentTrack.equalsIgnoreCase(lastTrack));
        }

        private boolean isPlayStateChanged(boolean currentlyPlaying, boolean lastPlayingState) {
            return currentlyPlaying != lastPlayingState;
        }
    };

}