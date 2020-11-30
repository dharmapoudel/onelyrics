package com.lyricslover.onelyrics.lyricsproviders;

import android.content.Context;

import com.lyricslover.onelyrics.pojos.Lyrics;
import com.lyricslover.onelyrics.pojos.Song;

public interface LyricsProvider {

    Lyrics provideLyrics(Song song, Context context);
}
