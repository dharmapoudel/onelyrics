package com.lyricslover.onelyrics.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.lyricslover.onelyrics.helpers.BitmapThumbnailHelper;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Song;

import java.lang.ref.WeakReference;

public class LocalAlbumArtDownloaderTask extends AsyncTask<Song, Void, Song> {

    protected static final String TAG = LocalAlbumArtDownloaderTask.class.getSimpleName();


    private final WeakReference<Bitmap> bitmapReference;
    private WeakReference<Context> contextRef;

    public LocalAlbumArtDownloaderTask(Bitmap bitmap, Context context) {
        bitmapReference = new WeakReference<>(bitmap);
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Song doInBackground(Song... params) {
        Song song = params[0];
        try {
            Utils.saveFileToStorage(BitmapThumbnailHelper.createThumbnailBitmap(bitmapReference.get(), 150, 150), song, contextRef.get());
        } catch (Exception e) {
            Log.e(TAG, "Exception occured while saving albumart" + e.getMessage());
        }
        return song;
    }

    @Override
    protected void onPostExecute(Song song) {
        //update the album art on list view
        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_LYRICS_DOWNLOADED));
    }


    @Override
    protected void onCancelled() {

    }


}