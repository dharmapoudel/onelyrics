
package com.lyricslover.onelyrics.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.lyricslover.onelyrics.lyricsproviders.LocalStorageLyricsProvider;
import com.lyricslover.onelyrics.lyricsproviders.LyricsProvider;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Lyrics;
import com.lyricslover.onelyrics.pojos.Song;

import org.jsoup.internal.StringUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FetchLyricsTask extends AsyncTask<Song, Void, Lyrics> {

    //private final WeakReference<View> lyricsPanelReference;

    //private SharedPreferences sharedPreferences;

    private static final String TAG = FetchLyricsTask.class.getSimpleName();

    private final Intent intent;

    //private final LyricsService service;
    private final WeakReference<Context> contextRef;

    //private AlbumArtDownloaderTask albumArtDownloaderTask;

    //private final Handler handler;

    private final AppPreferences appPreferences;

    public FetchLyricsTask(Intent i, Context context) {
        //lyricsPanelReference = new WeakReference<>(bottomLayout);
        contextRef = new WeakReference<>(context);
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(bottomLayout.getContext());
        intent = i;
        //this.service = service;
        //this.handler = handler;

        appPreferences = new AppPreferences(context);
    }

    @Override
    protected Lyrics doInBackground(Song... params) {
        Lyrics lyrics = null;
        boolean cacheLyrics = appPreferences.getBoolean(Constants.CACHE_LYRICS, true);

        //boolean isMusicPlaying = intent.getBooleanExtra("isMusicPlaying", false);

        Song song = new Song(params[0]);
        Context context = contextRef.get();

        try {

            //fetch lyrics from device if available else download
            //if(isMusicPlaying || offlineRead) {
            lyrics = fetchLyrics(song, context);
            //}

            //set  offline lyrics not available
            appPreferences.setPreference(Constants.ACTION_LYRICS_DOWNLOADED, false);

            //save lyrics to local storage if not available offline
            if (lyrics != null && !StringUtil.isBlank(lyrics.getText()) && cacheLyrics) {
                Utils.writeToFile(lyrics.getText(), Utils.getFileFromSong(lyrics.getType(), params[0], context));
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception occurred while retrieving lyrics");
        }

        return lyrics;
    }


    @Override
    protected void onPostExecute(Lyrics lyrics) {
        if (isCancelled()) {
            lyrics = null;
        }

        //boolean offlineRead = intent.getBooleanExtra(Constants.OFFLINE_READ, false);

        //set if offline lyrics is available if user is not reading offline
        //if (!offlineRead) {
        appPreferences.setPreference(Constants.ACTION_LYRICS_DOWNLOADED, true);

        intent.setAction(Constants.ACTION_LYRICS_DOWNLOADED);
        intent.putExtra(Constants.ACTION_LYRICS_DOWNLOADED, lyrics != null && !StringUtil.isBlank(lyrics.getText()));
        LocalBroadcastManager.getInstance(contextRef.get()).sendBroadcast(intent);
        //}

    }


    private Lyrics fetchLyrics(Song song, Context context) {
        //get offline lyrics by default
        Lyrics lyrics = (new LocalStorageLyricsProvider()).provideLyrics(song, context);

        //duplicate the song object for lookup by single artist in case where artist name has comma, ft, etc.
        Song song1 = new Song(song);
        song1.setArtist(Utils.getShortName(song.getArtist()));
        song1.setTrack(Utils.getShortName(song.getTrack()));

        boolean productID = appPreferences.getPreference(Constants.PRODUCT_ID, false);


        return lyrics;
    }

    private Lyrics fetchLyrics(Song song, Context context, List<LyricsProvider> lyricProviders) {
        Lyrics lyrics = null;
        Collections.shuffle(lyricProviders);

        for (LyricsProvider provider : lyricProviders) {
            lyrics = provider.provideLyrics(song, context);
            if (!StringUtil.isBlank(lyrics.getText()))
                break;
        }
        return lyrics;
    }



}