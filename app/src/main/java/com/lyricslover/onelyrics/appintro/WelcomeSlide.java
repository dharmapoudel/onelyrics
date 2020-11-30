package com.lyricslover.onelyrics.appintro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lyricslover.onelyrics.R;

import androidx.annotation.Nullable;
import io.github.dreierf.materialintroscreen.SlideFragment;

public class WelcomeSlide extends SlideFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_slide, container, false);
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
        return true;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.error_message);
    }
}