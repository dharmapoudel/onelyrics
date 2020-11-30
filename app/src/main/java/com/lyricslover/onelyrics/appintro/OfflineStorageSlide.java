package com.lyricslover.onelyrics.appintro;

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

public class OfflineStorageSlide extends SlideFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View slide = inflater.inflate(R.layout.fragment_offlinestorage_slide, container, false);
        Button storageButton = slide.findViewById(R.id.button_storage);

        if (PermissionHelper.isStoragePermissionAvailable(getActivity())) {
            Utils.disableButton(storageButton);
        }

        storageButton.setOnClickListener((v) ->  PermissionHelper.requestPermissions(getActivity(), Constants.WRITE_EXTERNAL));

        return slide;
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
        return PermissionHelper.isStoragePermissionAvailable(getActivity());
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.error_message);
    }
}