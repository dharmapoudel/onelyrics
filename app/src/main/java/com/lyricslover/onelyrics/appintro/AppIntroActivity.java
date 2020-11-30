package com.lyricslover.onelyrics.appintro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.helpers.PermissionHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.dreierf.materialintroscreen.MaterialIntroActivity;

public class AppIntroActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        enableLastSlideAlphaExitTransition(true);


        addSlide(new WelcomeSlide());
        addSlide(new OverlaySlide());
        addSlide(new OfflineStorageSlide());
        addSlide(new WorksWithAllSlide());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (!PermissionHelper.allPermissionsAvailable(this)) {
            Toast.makeText(getApplicationContext(), R.string.provide_all_permissions, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AppIntroActivity.class);
            startActivityForResult(intent, Constants.OVERLAY_ONTOP);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.WRITE_EXTERNAL && PermissionHelper.isStoragePermissionAvailable(AppIntroActivity.this)) {
            Utils.disableButton(findViewById(R.id.button_storage));
        }

    }


}