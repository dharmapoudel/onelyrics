package com.lyricslover.onelyrics.misc;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.pojos.AppPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class ThemePreferenceDialog extends AlertDialog {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String selectedTheme;
    private String title;
    AppPreferences prefs;
    private int mClickedDialogEntryIndex;
    private Context context;

    public ThemePreferenceDialog(@NonNull Context context, AppPreferences appPreferences) {
        super(context);
        this.context = context;
        prefs = appPreferences;
        //prefs = appPreferences.initSharedPreferences(context); //PreferenceManager.getDefaultSharedPreferences(context);
        mEntries = context.getResources().getStringArray(R.array.themeName);
        mEntryValues = context.getResources().getStringArray(R.array.themeValues);
        selectedTheme = prefs.getPreference(Constants.PREF_APP_THEME, "1");
        title = context.getString(R.string.app_theme_summary);
    }

    public void createDialog() {
        Builder builder = new Builder(context, R.style.OneLyricsDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.themes_dialog, null);
        builder.setView(dialogView);
        builder.setTitle(title);

        //
        builder.setSingleChoiceItems(mEntries, getValueIndex(), (DialogInterface dialog, int which) -> {
            mClickedDialogEntryIndex = which;
            selectedTheme = mEntryValues[mClickedDialogEntryIndex].toString();
            prefs.setPreference(Constants.PREF_APP_THEME, selectedTheme);
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int getValueIndex() {
        if (selectedTheme != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(selectedTheme)) {
                    return i;
                }
            }
        }
        return -1;
    }

}