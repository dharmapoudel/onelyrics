package com.lyricslover.onelyrics.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.services.LyricsService;

import androidx.appcompat.app.AppCompatActivity;

public class ShortcutActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize app preferences
        AppPreferences appPreferences = new AppPreferences(getApplicationContext());

        Context context = getApplicationContext();
        Song song = appPreferences.getSongFromPreferences();

        Intent showLyricsPanelIntent = new Intent(context, LyricsService.class);
        showLyricsPanelIntent.putExtra(Constants.ARTIST, song == null ? "" : song.getArtist());
        showLyricsPanelIntent.putExtra(Constants.TRACK, song == null ? "" : song.getTrack());
        showLyricsPanelIntent.putExtra(Constants.POSITION, song == null ? "" : song.getPosition());
        showLyricsPanelIntent.putExtra(Constants.DURATION, song == null ? "" : song.getDuration());
        showLyricsPanelIntent.putExtra(Constants.OPEN_LYRICS_PANEL, true);
        showLyricsPanelIntent.putExtra(Constants.SHORTCUT_LYRICS_PANEL, true);

        startService(showLyricsPanelIntent);

        finish();
    }


}