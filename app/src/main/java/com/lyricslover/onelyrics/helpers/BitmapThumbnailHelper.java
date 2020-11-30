package com.lyricslover.onelyrics.helpers;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;

/**
 * Video capture Bitmap
 */
public class BitmapThumbnailHelper {


	public static Bitmap createThumbnail(String pathName, int newWidth, int newHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		int oldWidth = options.outWidth;
		int oldHeight = options.outHeight;

		int ratioWidth, ratioHeight;

		if (newWidth != 0 && newHeight == 0) {
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = ratioWidth;
		} else if (newWidth == 0 && newHeight != 0) {
			ratioHeight = oldHeight / newHeight;
			options.inSampleSize = ratioHeight;
		} else {
			ratioHeight = oldHeight / newHeight;
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = Math.max(ratioHeight, ratioWidth);
		}
		options.inPreferredConfig = Config.ALPHA_8;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
    public static Bitmap createThumbnailBitmap(Bitmap bitmap, int width, int height) {
        int sIconWidth  = width;
        int sIconHeight = height;

        final Paint sPaint = new Paint();
        final Rect sBounds = new Rect();
        final Rect sOldBounds = new Rect();
        Canvas sCanvas = new Canvas();


        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));

        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (width > 0 && height > 0) {
            if (width < bitmapWidth || height < bitmapHeight) {
                final float ratio = (float) bitmapWidth / bitmapHeight;

                if (bitmapWidth > bitmapHeight) {
                    height = (int) (width / ratio);
                } else if (bitmapHeight > bitmapWidth) {
                    width = (int) (height * ratio);
                }

                final Bitmap.Config c = (width == sIconWidth && height == sIconHeight) ? bitmap
                        .getConfig() : Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth,
                        sIconHeight, c);
                sCanvas.setBitmap(thumb);
                sPaint.setDither(false);
                sPaint.setFilterBitmap(true);
                sBounds.set((sIconWidth - width) / 2,
                        (sIconHeight - height) / 2, width, height);
                sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
                sCanvas.drawBitmap(bitmap, sOldBounds, sBounds, sPaint);
                return thumb;
            } else if (bitmapWidth < width || bitmapHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth,
                        sIconHeight, c);
                sCanvas.setBitmap(thumb);
                sPaint.setDither(false);
                sPaint.setFilterBitmap(true);
                sCanvas.drawBitmap(bitmap, (sIconWidth - bitmapWidth) / 2,
                        (sIconHeight - bitmapHeight) / 2, sPaint);
                return thumb;
            }
        }

        return bitmap;
    }

	public static Bitmap createAlbumThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			byte[] art = retriever.getEmbeddedPicture();
			bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
		} catch (Exception ex) {
		    //do nothing
		} finally {
            retriever.release();
            retriever.close();
		}
		return bitmap;
	}
}