package com.lyricslover.onelyrics.appintro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.helpers.PermissionHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;

import androidx.annotation.Nullable;
import io.github.dreierf.materialintroscreen.SlideFragment;

public class WorksWithAllSlide extends SlideFragment {
    private Button notificationButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View slide = inflater.inflate(R.layout.fragment_workswithall_slide, container, false);
        notificationButton = slide.findViewById(R.id.button_notification);


        if (PermissionHelper.isNotificationPermissionAvailable(notificationButton.getContext())) {
            Utils.disableButton(notificationButton);
        }

        notificationButton.setOnClickListener((v) -> startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), Constants.NOTIFICATION_ACCESS));

        return slide;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.NOTIFICATION_ACCESS && PermissionHelper.isNotificationPermissionAvailable(notificationButton.getContext())) {
            Utils.disableButton(notificationButton);
        }
    }

    @Override
    public int backgroundColor() {
        return R.color.colorPrimary;
    }

    @Override
    public int buttonsColor() {
        return R.color.colorAccent;
    }

    @Override
    public boolean canMoveFurther() {
        return PermissionHelper.isNotificationPermissionAvailable(notificationButton.getContext());
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.error_message);
    }
}