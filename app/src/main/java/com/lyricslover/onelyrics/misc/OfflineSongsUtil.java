package com.lyricslover.onelyrics.misc;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class OfflineSongsUtil {

    private static final String TAG = OfflineSongsUtil.class.getSimpleName();

    public static HashMap<String, Song> convertListToHashMap(ArrayList<Song> songArrayList){
        HashMap<String, Song> offlineSongsMap = new HashMap<>();
        for(Song song : songArrayList){
            offlineSongsMap.put(Utils.getSongTitle(song), song);
        }
        return offlineSongsMap;
    }



    public static void getOfflineSongs(ContentResolver contentResolver, List<Song> songArrayList) {

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        if(cursor == null) return;

        while (cursor.moveToNext()) {
            String artist = cursor.getString(1);
            String title = cursor.getString(2);
            long songID = Long.parseLong(cursor.getString(0));
            String durationString = cursor.getString(3);
            durationString  = (durationString == null || durationString.equalsIgnoreCase("0"))? "1" : durationString;

            long duration = Long.parseLong(durationString);
            if ((duration / 1000) > 40) {
                songArrayList.add(new Song(title, artist, songID));
            }

        }
        cursor.close();

    }

    @TargetApi(Build.VERSION_CODES.O)
    public static List<Song> getOfflineLyrics(Context context, AppPreferences appPreferences){
        //String dir = Utils.getLyricsDirectoryPath(context);
        File filePath = Utils.getLyricsDirectory(context); // new File(dir);


        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        File[] filesArray = filePath.listFiles((d, f) ->   f.contains(".txt") || f.contains(".lrc"));


        ArrayList<Song> list = new ArrayList<>();
        if(filesArray != null) {
            for (File file : filesArray) {
                String fileName = file.getName().replace(".txt", "").replace(".lrc", "");
                String[] st = fileName.split("-");
                if (st.length > 1) {
                    String track = st[0].replaceAll("_", " ");
                    String artist = st[1].replaceAll("_", " ");
                    list.add(new Song(track, artist, file.getAbsolutePath(), file.lastModified()));
                }
            }
        }

        //sorting
        if (list.size() > 1) {
            String sortType = appPreferences.getPreference(Constants.PREF_SORT_TYPE, Constants.PREF_SORT_TYPE_DATE);
            boolean sortOrder = appPreferences.getPreference(Constants.PREF_SORT_ORDER, true);

            switch(sortType){

                //sort by track
                case Constants.PREF_SORT_TYPE_TRACK:
                    Collections.sort(list, (a, b) ->  sortOrder ? b.getTrack().compareToIgnoreCase(a.getTrack()) :  a.getTrack().compareToIgnoreCase(b.getTrack()));
                    break;

                //sort by artist
                case Constants.PREF_SORT_TYPE_ARTIST:
                    Collections.sort(list, (a, b) ->  sortOrder ? b.getArtist().compareToIgnoreCase(a.getArtist()) :  a.getArtist().compareToIgnoreCase(b.getArtist()));
                    break;

                //sort by last modified date
                case Constants.PREF_SORT_TYPE_DATE:
                default:
                    Collections.sort(list, (a, b) -> sortOrder ?  Long.compare(b.getLastModified(), a.getLastModified()) : Long.compare(a.getLastModified(), b.getLastModified()));
                    break;
            }
        }

        return list;
    }
}