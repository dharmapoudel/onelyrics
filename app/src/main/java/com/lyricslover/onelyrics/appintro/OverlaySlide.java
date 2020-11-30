package com.lyricslover.onelyrics.appintro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

public class OverlaySlide extends SlideFragment {
    private Button overlayButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View slide = inflater.inflate(R.layout.fragment_overlay_slide, container, false);
        overlayButton = slide.findViewById(R.id.button_overlay);

        if (PermissionHelper.isOverlayPermissionAvailable(getContext())) {
            Utils.disableButton(overlayButton);
        }

        overlayButton.setOnClickListener(v -> {
            if(getContext().getPackageName() != null) {
                final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, Constants.OVERLAY_ONTOP);
            }
        });

        return slide;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.OVERLAY_ONTOP) {
            if (PermissionHelper.isOverlayPermissionAvailable(getContext())) {
                Utils.disableButton(overlayButton);
            }
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
        return PermissionHelper.isOverlayPermissionAvailable(getContext());
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.error_message);
    }
}