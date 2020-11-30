package com.lyricslover.onelyrics.albumartproviders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lyricslover.onelyrics.helpers.BitmapThumbnailHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Song;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class FanArtArtistImageProvider implements  AlbumArtProvider{

    private static String TAG = FanArtArtistImageProvider.class.getSimpleName();
    private static String MUSICBRAINZ_API_URL = "http://musicbrainz.org/ws/2/artist/";
    private static String API_KEY = "c0cc5d1b6e807ce93e49d75e0e5d371b";
    private static String FANART_URL = "http://webservice.fanart.tv/v3/music/";

    @Override
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
        String artistId = getArtistId(song);
        if(!StringUtil.isBlank(artistId) ){
            String artistArtUrl = getAlbumArtUrl(artistId);
            if(!StringUtil.isBlank(artistArtUrl) ) {
                saveAlbumArtFile(song, artistArtUrl, context);
                Bitmap bitmap = BitmapThumbnailHelper.createThumbnail(Utils.getFilePathFromSong(song, Constants.FILE_TYPE_JPG, context), 200, 200);
                Utils.saveFileToStorage(bitmap, song, context);
            }
        }
    }


    public  String getArtistId(Song song) {

        String artistId = "";

        String url = MUSICBRAINZ_API_URL + "?query=artist:" + Utils.getShortName(song.getArtist()) + "&limit=1";
        try {
            Document document =  Jsoup
                    .connect(url)
                    //.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .timeout(10000).get();
            artistId = document.select("artist-list > artist").first().attr("id");
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
        }
        return artistId;
    }

    public  String getAlbumArtUrl(String artistID) {
        String fanArtUrl = FANART_URL + artistID + "/?api_key=" + API_KEY;
        String artistArtUrl = "";
        try{
            String jsonResponse = Jsoup.connect(fanArtUrl)
                    //.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .timeout(20000).ignoreContentType(true).execute().body();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if(jsonObject.has("artistthumb"))
                artistArtUrl = ((JSONObject) jsonObject.getJSONArray("artistthumb").get(0)).getString("url");
            if(StringUtil.isBlank(artistArtUrl) && jsonObject.has("artistbackground"))
                artistArtUrl = ((JSONObject) jsonObject.getJSONArray("artistbackground").get(0)).getString("url");

        } catch (HttpStatusException e) {
            e.printStackTrace();
            Log.e(TAG, "HttpStatusException occured while getting lyrics from lyrics fan.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException occured while getting lyrics from lyrics fan.");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "NullPointerException occured while getting lyrics from lyrics fan.");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException occured while getting lyrics from lyrics fan.");
        }
        Log.i(TAG, artistArtUrl);
        return artistArtUrl;
    }


    private void saveAlbumArtFile(Song song, String albumArtUrl, Context context){
        String fileName = Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_JPG);
        Utils.downloadFileFromURL(fileName, albumArtUrl, context);

    }


}
