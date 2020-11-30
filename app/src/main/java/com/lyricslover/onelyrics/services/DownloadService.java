package com.lyricslover.onelyrics.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.helpers.NotificationHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Song;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DownloadService extends Service {

    ArrayList<Song> songArrayList = new ArrayList<>();
    int progress = 1;
    int count;

    Handler handler;


    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(final Intent intent, final int flags, int startId) {

        try { // try - catch for a crash which occurs if the user removes Lyrically from the recents while the lyrics are being downloaded
            songArrayList = intent.getParcelableArrayListExtra(Constants.SONGS);
            count = (songArrayList != null) ?  songArrayList.size() : 0;
        } catch (NullPointerException e) {
            stopSelf();
            return START_NOT_STICKY;
        }


        // handler to update the notification progress bar
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (count == progress) {
                    stopSelf();
                    new NotificationHelper(getApplicationContext()).sendNotificationForBulkDownload( 0, 0, getResources().getString(R.string.lyrics_downloaded), "", getString(R.string.downloadingLyrics));
                } else {
                    new NotificationHelper(getApplicationContext()).sendNotificationForBulkDownload( count, progress, getResources().getString(R.string.downloadingLyrics) + " (" + progress + "/" + (count) + ") " + " " + Utils.getSongTitle(songArrayList.get(progress-1)),  "", getString(R.string.downloadingLyrics));
                }
                progress++;
            }
        };

        Messenger messenger = new Messenger(handler);
        for (Song song : songArrayList) {
            Intent intent1 = new Intent(this, FetchLyrics.class);
            intent1.putExtra(Constants.TRACK, song.getTrack());
            intent1.putExtra(Constants.MESSENGER, messenger);
            intent1.putExtra(Constants.ARTIST, song.getArtist());
            intent1.putExtra(Constants.SONG_ID, song.getId());
            startService(intent1);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        handler = null;
    }
}
