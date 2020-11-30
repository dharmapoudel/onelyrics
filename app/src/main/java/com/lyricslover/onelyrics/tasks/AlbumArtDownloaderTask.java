package com.lyricslover.onelyrics.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.lyricslover.onelyrics.albumartproviders.AlbumArtProvider;
import com.lyricslover.onelyrics.albumartproviders.BingAlbumArtProvider;
import com.lyricslover.onelyrics.albumartproviders.FanArtArtistImageProvider;
import com.lyricslover.onelyrics.albumartproviders.LastFmAlbumArtProvider;
import com.lyricslover.onelyrics.albumartproviders.LocalStorageAlbumArtProvider;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.Song;

import org.jsoup.internal.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlbumArtDownloaderTask extends AsyncTask<Song, Void, Drawable> {

    protected static final String TAG = AlbumArtDownloaderTask.class.getSimpleName();
    private Intent intent;

    //private final WeakReference<View> lyricsPanelReference;
    private final WeakReference<Context> contextRef;
    //private final AppPreferences appPreferences;

    public AlbumArtDownloaderTask(Intent i, Context context) {
        //this.appPreferences = AppPreferences.getInstance(v.getContext());
        //lyricsPanelReference = new WeakReference<>(v);
        contextRef = new WeakReference<>(context);
        intent = i;
    }

    /*@Override
    protected void onPreExecute() {
    }*/

    @Override
    protected Drawable doInBackground(Song... params) {
        Song song = params[0];
        Drawable albumArt = null;
        try {
            albumArt = fetchAlbumArt(getAlbumArtProviderList(), song, contextRef.get());
        } catch (NullPointerException e) {
            Log.e(TAG, "Null Pointer exception");
        }
        return albumArt;
    }

    private Drawable fetchAlbumArt(List<AlbumArtProvider> albumArtProviderList, Song song, Context context) {
        Drawable albumArt = (new LocalStorageAlbumArtProvider()).provideAlbumArt(song, context);
        //boolean offlineRead = intent.getBooleanExtra(Constants.OFFLINE_READ, false);
        if (albumArt == null) {
            for (AlbumArtProvider provider : albumArtProviderList) {
                albumArt = provider.provideAlbumArt(song, context);
            /*if(null != albumArt){
                //update the album art on list view
                if(!offlineRead) {
                    LocalBroadcastManager.getInstance(lyricsPanelReference.get().getContext()).sendBroadcast(new Intent("lyrics_downloaded"));
                }
                break;
            }*/

            }
        }
        return albumArt;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if (isCancelled()) {
            drawable = null;
        }

        //update the album art on list view
        //boolean offlineRead = intent.getBooleanExtra(Constants.OFFLINE_READ, false);

        //if (drawable != null ) { //&& !offlineRead) {
        intent.setAction(Constants.ACTION_ALBUMART_DOWNLOADED);
        intent.putExtra(Constants.ACTION_ALBUMART_DOWNLOADED, drawable != null);
        LocalBroadcastManager.getInstance(contextRef.get()).sendBroadcast(intent);
        //add some blur to the lyrics panel
        // blurLyricsPanel(Utils.getSongFromIntent(intent), lyricsPanelReference.get());
        // }
    }

    /*@Override
    protected void onCancelled() {

    }*/

    /*private void blurLyricsPanel(Song song, View lyricsPanel){
        Context context = lyricsPanel.getContext();
        Resources resources = context.getResources();

        lyricsPanel.findViewById(R.id.panelContent).setBackground(null);
        if (appPreferences.getPreference(Constants.PANEL_BLUR, false) && song != null) {
            Bitmap image = (new BlurHelper(context)).blur(Utils.getAlbumArtBitmap(song, context));
            lyricsPanel.setBackground(new BitmapDrawable(resources, image));
        } else {
            lyricsPanel.setBackgroundColor(resources.getColor(R.color.listDivider_dark));
        }
        lyricsPanel.getBackground().setAlpha((int) (appPreferences.getPanelAlpha() * 2.55));
    }*/

    private List<AlbumArtProvider> getAlbumArtProviderList() {
        List<AlbumArtProvider> albumArtProviders = new ArrayList<>();
        //albumArtProviders.add(new LocalStorageAlbumArtProvider());
        albumArtProviders.add(new LastFmAlbumArtProvider());
        albumArtProviders.add(new FanArtArtistImageProvider());
        albumArtProviders.add(new BingAlbumArtProvider());
        return albumArtProviders;
    }

}