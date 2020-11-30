package com.lyricslover.onelyrics.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.services.LyricsService;

public class SystemDialogCloseReceiver extends BroadcastReceiver {


    //private static final String tag = SystemDialogCloseReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        // hide lyrics panel when home or recent buttons are clicked

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(Constants.SYSTEM_DIALOG_REASON_KEY);
            if (reason != null && (reason.equals(Constants.SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals(Constants.SYSTEM_DIALOG_REASON_RECENT_APPS))) {
                Intent intent1 = new Intent(context, LyricsService.class);
                intent1.putExtra(Constants.CLOSE_LYRICS_PANEL, true);
                context.startService(intent1);
            }
        }
    }
}