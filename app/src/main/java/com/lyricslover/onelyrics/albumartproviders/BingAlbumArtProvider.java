package com.lyricslover.onelyrics.albumartproviders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static com.lyricslover.onelyrics.misc.Utils.getLyricsDirectory;

public class BingAlbumArtProvider implements AlbumArtProvider {

    private static final String TAG = BingAlbumArtProvider.class.getSimpleName();

    @Override
    public Drawable provideAlbumArt(Song song, Context context) {
        Drawable albumArt = (new LocalStorageAlbumArtProvider()).provideAlbumArt(song, context);
        if (albumArt == null) {
            albumArt = getAlbumArt(song, context);
        }
        return albumArt;
    }

    private Drawable getAlbumArt(Song song, Context context) {
        Drawable drawable = null;
        File path = getLyricsDirectory(context);
        String artist = song.getArtist();
        String track = song.getTrack();
        File albumArtFile = new File(path, Utils.getFileName(track, artist, Constants.FILE_TYPE_PNG));
        try {
            Document document = getPageElementFromBing(song);
            String albumArtUrl = getAlbumArtUrlFromBing(document);
            downloadAlbumArt(albumArtUrl, albumArtFile);

            Drawable albumArtDrawable = Utils.getAlbumArtDrawable(song, context);
            if (albumArtDrawable != null) {
                drawable = albumArtDrawable;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "NullPointerException");
        }
        return drawable;
    }

    public static Document getPageElementFromBing(Song song) throws IOException {
        String url = "https://www.bing.com/search?q=" + Utils.getShortName(song.getArtist()) + " " + Utils.getShortName(song.getTrack()) + " lyrics";
        Log.i(TAG, url);
        return Jsoup.connect(url).timeout(20000).get();
    }

    public static String getAlbumArtUrlFromBing(Document document) throws UnsupportedEncodingException {
        String albumArtUrl = "";
        Element imageElem = document.select("#b_context  .cico > .rms_iac").first();

        String imageSrc = "";
        if (imageElem != null) {
            imageSrc = imageElem.attr("data-src");
            imageSrc = java.net.URLDecoder.decode(imageSrc, StandardCharsets.UTF_8.name());
        }

        if (!imageSrc.equals("")) {
            albumArtUrl = "https://www.bing.com" + imageSrc;
        }
        Log.i(TAG, albumArtUrl);
        return albumArtUrl;
    }

    private static void downloadAlbumArt(String albumArtUrl, File albumArtFile) {
        try {
            URL url = new URL(albumArtUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(albumArtFile);

            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException");
        }
    }
}
