

package com.lyricslover.onelyrics.helpers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.main.MainActivity;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.services.LyricsService;
import com.lyricslover.onelyrics.services.ShowLyrics;

import androidx.core.app.NotificationCompat;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {

    private NotificationManager manager;


    public NotificationHelper(Context ctx) {
        super(ctx);
    }

    @TargetApi(27)
    private void createChannel() {

        // primary channel
        NotificationChannel chan1 = new NotificationChannel(Constants.PRIMARY_CHANNEL,
                getString(R.string.noti_channel_first), NotificationManager.IMPORTANCE_MIN);

        chan1.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        chan1.enableLights(true);
        chan1.setLightColor(R.color.colorPrimary);
        chan1.setShowBadge(true);
        chan1.setSound(null, null);
        getManager().createNotificationChannel(chan1);


        //secondary channel
        NotificationChannel chan2 = new NotificationChannel(Constants.SECONDARY_CHANNEL,
                getString(R.string.noti_channel_second), NotificationManager.IMPORTANCE_DEFAULT);

        chan2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        chan2.enableLights(true);
        chan2.setLightColor(R.color.colorPrimary);
        chan2.setShowBadge(true);
        chan2.setSound(null, null);
        getManager().createNotificationChannel(chan2);

    }


    public void sendNotification(Intent intent, Handler handler) {
        Context context = getApplicationContext();
        Song song = Utils.getSongFromIntent(intent);

        Intent showLyricsIntent = new Intent(this, ShowLyrics.class);
        showLyricsIntent.putExtra("messenger", new Messenger(handler));
        showLyricsIntent.putExtra("song", Utils.getSongFromIntent(intent));
        showLyricsIntent.putExtra(Constants.OPEN_LYRICS_PANEL, intent.getBooleanExtra(Constants.OPEN_LYRICS_PANEL, false));
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, showLyricsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent startAppIntent = new Intent(context, MainActivity.class);
        //startAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent startAppPendingIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent1 = new Intent(context, LyricsService.class);
        intent1.putExtra(Constants.CLOSE_LYRICS_PANEL, true);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(context, 0, intent1, 0);

        //PendingIntent pi = (song != null) ? pendingIntent : startAppPendingIntent;

        //boolean isMusicPlaying = intent.getBooleanExtra(Constants.IS_MUSIC_PLAYING, false);
        boolean offlineReading = intent.getBooleanExtra(Constants.OFFLINE_READ, false);

        String artist = (song != null) ? song.getArtist() : getResources().getString(R.string.app_running);
        String track = (song != null) ? song.getTrack() : null;
        String subText = null;
        if (offlineReading) {
            subText = getResources().getString(R.string.reading_offline);
        }

        sendNotification(track, artist, subText, pendingIntent, stopServicePendingIntent);

        //remove the notification when music is not playing
        /*if (!isMusicPlaying && !offlineReading) {
            cancelNotification(Constants.NOTIFICATION_ID);
        }*/

    }

    private void sendNotification(String track, String artist, String subText, PendingIntent pendingIntent, PendingIntent stopServicePendingIntent) {
        /*if (!Utils.isO()) {
            NotificationCompat.Builder nb = notificationBuilder(track, artist, subText, Constants.SECONDARY_CHANNEL);
            nb.setContentIntent(pendingIntent);
            nb.setDeleteIntent(stopServicePendingIntent);
            notify(Constants.NOTIFICATION_ID, nb);
        } else {*/
        NotificationCompat.Builder nb = notificationBuilder(track, artist, subText, Constants.SECONDARY_CHANNEL);
        nb.setContentIntent(pendingIntent);
        nb.setDeleteIntent(stopServicePendingIntent);
        notify(Constants.NOTIFICATION_ID, nb);
        //}
    }

    public void sendNotificationForBulkDownload(int count, int progress, String title, String content, String subText) {
        //send notification for offline reading

        Intent startAppIntent = new Intent(getApplicationContext(), MainActivity.class);
        startAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        /*if (!Utils.isO()) {
            NotificationCompat.Builder nb = notificationBuilder(title, content, subText, Constants.SECONDARY_CHANNEL);
            nb.setContentIntent(startAppPendingIntent);
            nb.setProgress(count, progress, false).setAutoCancel(true);
            notify(Constants.NOTIFICATION_ID + 2, nb);
        } else {*/
        NotificationCompat.Builder nb = notificationBuilder(title, content, subText, Constants.SECONDARY_CHANNEL);
        nb.setContentIntent(startAppPendingIntent);
        nb.setProgress(count, progress, false).setAutoCancel(true);
        notify(Constants.NOTIFICATION_ID + 2, nb);
        //}
    }


    /*public NotificationCompat.Builder notificationBuilderPreO(String track, String artist, String channel) {

        return new NotificationCompat.Builder(getApplicationContext(), (null == channel) ? Constants.PRIMARY_CHANNEL : channel)
                .setContentTitle((null == track) ? getResources().getString(R.string.app_name) : track)
                .setContentText((null == artist) ? getResources().getString(R.string.default_notification_content_text) : artist)
                .setColorized(true)
                .setGroup(Constants.GROUP_KEY)
                .setSmallIcon(R.drawable.notification_icon)
                .setOngoing(false)
                .setAutoCancel(false);
    }*/

    public NotificationCompat.Builder notificationBuilder(String track, String artist, String subText, String channel) {
        if (Utils.isO()) { createChannel(); }

        return new NotificationCompat.Builder(getApplicationContext(), (null == channel) ? Constants.PRIMARY_CHANNEL : channel)
                .setSubText((null == subText) ? "" : subText)
                .setContentTitle((null == track) ? getResources().getString(R.string.app_name) : track)
                .setContentText((null == artist) ? getResources().getString(R.string.default_notification_content_text) : artist)
                .setColorized(true)
                .setGroup(Constants.GROUP_KEY)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(getResources().getColor(R.color.colorPrimary, null))
                .setOngoing(false)
                .setAutoCancel(false);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void cancelNotification(int notificationId) {
        getManager().cancel(notificationId);
    }
}