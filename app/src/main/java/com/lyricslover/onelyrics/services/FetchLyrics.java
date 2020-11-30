package com.lyricslover.onelyrics.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.lyricslover.onelyrics.albumartproviders.AlbumArtProvider;
import com.lyricslover.onelyrics.albumartproviders.LastFmAlbumArtProvider;
import com.lyricslover.onelyrics.albumartproviders.LocalStorageAlbumArtProvider;
import com.lyricslover.onelyrics.lyricsproviders.LocalStorageLyricsProvider;
import com.lyricslover.onelyrics.lyricsproviders.LyricsProvider;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Lyrics;
import com.lyricslover.onelyrics.pojos.Song;

import org.jsoup.internal.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class FetchLyrics extends IntentService {

    private static final String TAG = LyricsService.class.getSimpleName();

    public FetchLyrics() {
        super("FetchLyrics");
    }

    String artist;
    String track;
    long songID;
    Messenger messenger;
    AppPreferences appPreferences;

    /*@Override
    public int onStartCommand(final Intent intent, final int flags, int startId) {
        if(intent.getBooleanExtra(Constants.STOP_BULK_DOWNLOAD, false)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }*/


    @Override
    protected void onHandleIntent(Intent intent) {

        /*if(intent.getBooleanExtra(Constants.STOP_BULK_DOWNLOAD, false)) {
            stopSelf();
        }*/
        appPreferences = AppPreferences.getInstance(this);

        boolean cacheLyrics = appPreferences.getPreference(Constants.CACHE_LYRICS, true);
        boolean cacheAlbumArt = appPreferences.getPreference(Constants.CACHE_ALBUMART, true);
        boolean offlineRead = intent.getBooleanExtra(Constants.OFFLINE_READ, false);

        artist = intent.getStringExtra(Constants.ARTIST);
        track = intent.getStringExtra(Constants.TRACK);
        songID = intent.getLongExtra(Constants.SONG_ID, 0);

        if (intent.getExtras() != null) {
            messenger = (Messenger) intent.getExtras().get(Constants.MESSENGER);
        }


        Song song = new Song(track, artist, songID);
        Lyrics lyrics;


        try {
            if (messenger != null) {
                messenger.send(new Message());
            }


            lyrics = fetchLyrics(getLyricsProviderList(song, getApplicationContext()), song);
            //save lyrics to local storage
            if (cacheLyrics && lyrics != null && !StringUtil.isBlank(lyrics.getText()) && !offlineRead) {
                Utils.writeToFile(lyrics.getText(), Utils.getFileFromSong(song, getApplicationContext()));

                //save album art to local storage
                if (cacheAlbumArt) {
                    fetchAlbumArt(getAlbumArtProviderList(), song);
                }
                //TODO: check on this later - maybe optimize?
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION_LYRICS_DOWNLOADED));
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "Remote exception occured while retrieving lyrics");
        } catch (Exception e) {
            Log.e(TAG, "Exception occured while retrieving lyrics");
        }
    }

    private Lyrics fetchLyrics(List<LyricsProvider> lyricsProviderList, Song song) {
        Lyrics lyrics = null;
        for (LyricsProvider provider : lyricsProviderList) {
            lyrics = provider.provideLyrics(song, getApplicationContext());
            if (!StringUtil.isBlank(lyrics.getText()))
                break;
        }
        return lyrics;
    }

    private void fetchAlbumArt(List<AlbumArtProvider> albumArtProviderList, Song song) {
        Drawable albumArt;
        for (AlbumArtProvider provider : albumArtProviderList) {
            albumArt = provider.provideAlbumArt(song, getApplicationContext());
            if (null != albumArt)
                break;
        }
    }

    private List<AlbumArtProvider> getAlbumArtProviderList() {
        List<AlbumArtProvider> albumArtProviders = new ArrayList<>();
        albumArtProviders.add(new LocalStorageAlbumArtProvider());
        albumArtProviders.add(new LastFmAlbumArtProvider());
        //albumArtProviders.add(new BingAlbumArtProvider());
        return albumArtProviders;
    }

    private List<LyricsProvider> getLyricsProviderList(Song song, Context context) {

        //get offline lyrics by default
        Lyrics lyrics = (new LocalStorageLyricsProvider()).provideLyrics(song, context);

        List<LyricsProvider> lyricProviders = new ArrayList<>();


        //lets shuffle so that we don't bog down a single provider
        if (!appPreferences.getPreference(Constants.PRODUCT_ID, false)) {
            Collections.shuffle(lyricProviders);
        }

        return lyricProviders;
    }


}