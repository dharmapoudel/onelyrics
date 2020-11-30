package com.lyricslover.onelyrics.albumartproviders;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.misc.Utils;

public class LocalStorageAlbumArtProvider implements AlbumArtProvider {
    @Override
    public Drawable provideAlbumArt(Song song, Context context) {
        return Utils.getAlbumArtDrawable(song, context);
    }
}
