package com.lyricslover.onelyrics.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.services.LyricsService;

import java.util.Objects;


public class MusicBroadcastReceiver extends BroadcastReceiver {

    //private static final String tag = MusicBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {


        boolean isPlaying = false;
        String artist;
        String track;
        long position;
        long duration;


        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        //shared preferences for currently playing song
        AppPreferences appPreferences = AppPreferences.getInstance(context);

        if (extras != null && action != null) {
            if (action.contains("meta")) {
                //current song metadata
                artist = intent.getStringExtra(Constants.ARTIST);
                track = intent.getStringExtra(Constants.TRACK);

                //damn u amazon I need another if condition
                if (Objects.equals(intent.getAction(), "com.amazon.mp3.metachanged")) {
                    artist = extras.getString("com.amazon.mp3.artist");
                    track = extras.getString("com.amazon.mp3.track");
                }

                //save the song data if its changed
                if (track != null && !track.equalsIgnoreCase(appPreferences.getPreference(Constants.TRACK, ""))) {
                    //save the data if action is metachanged so that we can use it for playstatechanged
                    appPreferences.setPreference(Constants.ARTIST, artist);
                    appPreferences.setPreference(Constants.TRACK, track);
                }

            } else if (action.contains("play")) {

                isPlaying = extras.getBoolean(extras.containsKey("playstate") ? "playstate" : "playing", false);

                position = (extras.get(Constants.POSITION) instanceof Long) ? extras.getLong(Constants.POSITION) : 0L;
                position = (extras.get(Constants.POSITION) instanceof Double) ? (long) extras.getDouble(Constants.POSITION) : position;

                Object durationExtra = extras.get(Constants.DURATION);
                duration = (durationExtra instanceof Long) ? (Long) durationExtra : 0;
                duration = (durationExtra instanceof Double) ? ((Double) durationExtra).longValue() : duration;
                duration = (durationExtra instanceof Float) ? ((Float) durationExtra).longValue() : duration;
                duration = (durationExtra instanceof Integer) ? (((Integer) durationExtra).longValue() * 1000) : duration;
                duration = (durationExtra instanceof String) ? (Double.valueOf((String) durationExtra)).longValue() : duration;


                //save position and duration
                if (isPlaying && position != appPreferences.getPreference(Constants.POSITION, 0L)) {
                    //save the data if action is metachanged so that we can use it for playstatechanged
                    appPreferences.setPreference(Constants.POSITION, position);
                    appPreferences.setPreference(Constants.DURATION, duration);
                }
            }


            //check if music is playing
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (!isPlaying && am != null) {
                isPlaying = am.isMusicActive();
            }


            //used saved data to send an intent to LyricsService
            Intent intent1 = new Intent(context, LyricsService.class);
            intent1.putExtra(Constants.IS_MUSIC_PLAYING, isPlaying);
            intent1.putExtra(Constants.ARTIST, appPreferences.getPreference(Constants.ARTIST, ""));
            intent1.putExtra(Constants.TRACK, appPreferences.getPreference(Constants.TRACK, ""));
            intent1.putExtra(Constants.POSITION, appPreferences.getPreference(Constants.POSITION, 0L));
            intent1.putExtra(Constants.DURATION, appPreferences.getPreference(Constants.DURATION, 0L));

            context.startService(intent1);
        }

    }

}
