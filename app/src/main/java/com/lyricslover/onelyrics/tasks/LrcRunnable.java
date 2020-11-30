package com.lyricslover.onelyrics.tasks;


import android.content.Context;
import android.os.Handler;

import com.lyricslover.onelyrics.lrcview.LrcView;

public class LrcRunnable implements Runnable {

    private boolean isMusicPlaying;
    private final LrcView lrcView;
    private long position;
    private final Context context;

    private boolean threadCancelled;

    private static LrcRunnable instance;

    private LrcRunnable(boolean isMusicPlaying, LrcView lrcView, long position, Context c) {
        this.isMusicPlaying = isMusicPlaying;
        this.lrcView = lrcView;
        this.position = position;
        this.context = c;
    }

    public static LrcRunnable getInstance(boolean isMusicPlaying, LrcView lrcView, long position, Context c) {
        if (instance == null) {
            instance = new LrcRunnable(isMusicPlaying, lrcView, position, c);
        }
        instance.isMusicPlaying = isMusicPlaying;

        return instance;
    }

    @Override
    public void run() {
        if (threadCancelled)
            return;

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(context.getMainLooper());

        mainHandler.post(() -> lrcView.onProgress(position));

        while (isMusicPlaying && !threadCancelled) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

            mainHandler.post(() -> {
                if (lrcView != null) {
                    lrcView.onProgress(position);
                    position += 200;
                }
            });

        }

    }


    /*public LrcView getLrcView() {
        return lrcView;
    }

    public void setLrcView(LrcView lrcView) {
        this.lrcView = lrcView;
    }*/

    public void setPosition(long position) {
        this.position = position;
    }

    public void setThreadCancelled(boolean threadCancelled) {
        this.threadCancelled = threadCancelled;
    }
}
