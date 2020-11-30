package com.lyricslover.onelyrics.albumartproviders;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lyricslover.onelyrics.pojos.Song;

public interface AlbumArtProvider {

    Drawable provideAlbumArt(Song song, Context context);
}
