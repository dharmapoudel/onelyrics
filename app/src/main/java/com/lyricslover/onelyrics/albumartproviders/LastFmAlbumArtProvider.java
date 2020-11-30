package com.lyricslover.onelyrics.albumartproviders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.misc.Utils;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class LastFmAlbumArtProvider implements AlbumArtProvider {

    protected static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    protected static final String API_KEY = "43bec89892e422a09413b52357dc23d9";
    protected static final String IMAGE_SIZE = "large";
    protected static final String TAG = "ImageDownloader";




    public Drawable provideAlbumArt(Song song, Context context) {
        Drawable albumArt = (new LocalStorageAlbumArtProvider()).provideAlbumArt(song, context);
        if (albumArt == null) {
            albumArt = getAlbumArt(song, context);
        }
        return albumArt;
    }

    protected Drawable getAlbumArt(Song song, Context context) {
        Drawable drawable = null;

        try{
            downloadAlbumArt(song, context);
            Drawable albumArtDrawable = Utils.getAlbumArtDrawable(song, context);
            if (albumArtDrawable != null) {
                drawable = albumArtDrawable;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Null Pointer exception");
        }
        return drawable;
    }

    private void downloadAlbumArt(Song song, Context context){
        String albumArtURL = getAlbumArtUrl(song);
        if(!StringUtil.isBlank(albumArtURL) ){
            saveAlbumArtFile(song, albumArtURL, context);
        }
    }

    private String getAlbumArtUrl(Song song) {

        String albumArtFileURL = "";

        String lastFmURL = BASE_URL+ "?method=track.getInfo&api_key=" + API_KEY + "&artist="+ Utils.getShortName(song.getArtist())
                +"&track="+ Utils.getShortName(song.getTrack()) +"&format=xml" + "&autocorrect=1" ;

        try {
            Document document =  Jsoup.connect(lastFmURL).timeout(10000).get();
            albumArtFileURL = document.select("album  image[size='"+IMAGE_SIZE+"']").first().text();
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
        }
        Log.i(TAG, albumArtFileURL);

        return albumArtFileURL;
    }

    private void saveAlbumArtFile(Song song, String albumArtUrl, Context context){
        String fileName = Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_PNG);
        Utils.downloadFileFromURL(fileName, albumArtUrl, context);
    }


}