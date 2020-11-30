package com.lyricslover.onelyrics.lyricsproviders;

import android.content.Context;
import android.util.Log;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.Lyrics;
import com.lyricslover.onelyrics.pojos.Song;


public class LocalStorageLyricsProvider implements  LyricsProvider {

    private static String TAG = LocalStorageLyricsProvider.class.getSimpleName();

    @Override
    public Lyrics provideLyrics(Song song, Context context) {
        return  getLyricsFromFile(song, context);
    }

    public Lyrics getLyricsFromFile(Song song, Context context) {
        String lyrics = "";
        Lyrics lyricsObject = null;
        try {
            boolean txtFileExists = Utils.fileExists(Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_TXT), context);
            boolean lrcFileExists = Utils.fileExists(Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_LRC), context);

            if (lrcFileExists) {
                lyrics = Utils.readFromFile(Utils.getFileFromSongByFileType(song, Constants.FILE_TYPE_LRC, context));
            } else if(txtFileExists){
                lyrics = Utils.readFromFile(Utils.getFileFromSongByFileType(song, Constants.FILE_TYPE_TXT,context));
            }
            lyricsObject =  new Lyrics(lyrics, lrcFileExists ? Constants.FILE_TYPE_LRC : Constants.FILE_TYPE_TXT);
        } catch(Exception e){
            e.printStackTrace();
           Log.e(TAG, "Exception Occured while getting lyrics from local file.");
        }
        return lyricsObject;
    }
}
