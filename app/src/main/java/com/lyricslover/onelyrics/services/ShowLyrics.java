package com.lyricslover.onelyrics.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.Song;


public class ShowLyrics extends IntentService {

    public ShowLyrics() {
        super("ShowLyrics");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getExtras() != null) {
            Song song = (Song) intent.getExtras().get("song");
            Messenger messenger = (Messenger) intent.getExtras().get(Constants.MESSENGER);
            try {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putParcelable("song", song);
                message.setData(bundle);
                if(messenger != null) messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }
}