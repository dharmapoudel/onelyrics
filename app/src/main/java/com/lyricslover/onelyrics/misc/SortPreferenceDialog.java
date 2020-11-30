package com.lyricslover.onelyrics.misc;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.main.MainActivity;
import com.lyricslover.onelyrics.pojos.AppPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class SortPreferenceDialog extends AlertDialog {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String sortType;
    private String title;
    private boolean sortOrder;
    //SharedPreferences prefs;
    private int mClickedDialogEntryIndex;
    //private int selectedTheme;
    private Context context;
    private AppPreferences appPreferences;

    public SortPreferenceDialog(@NonNull Context context, AppPreferences appPreferences) {
        super(context);
        this.context = context;
        this.appPreferences = appPreferences;

        mEntries = context.getResources().getStringArray(R.array.sortEntries);
        mEntryValues = context.getResources().getStringArray(R.array.sortValues);
        sortType = appPreferences.getPreference(Constants.PREF_SORT_TYPE, Constants.PREF_SORT_TYPE_DATE);
        sortOrder = appPreferences.getPreference(Constants.PREF_SORT_ORDER, true);
        title = context.getString(R.string.menu_sort);
        //selectedTheme = Utils.getCurrentTheme((new AppPreferences()).getPreferenceStringByKey(context, Constants.PREF_APP_THEME, "1"));
    }

    public void createDialog(MainActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.OneLyricsDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.permissions_dialog, null);
        builder.setView(dialogView);
        builder.setTitle(title);

        //set the checkbox
        CheckBox checkBox = dialogView.findViewById(R.id.sortOrder);
        checkBox.setChecked(sortOrder);

        //
        builder.setSingleChoiceItems(mEntries, getValueIndex(), (DialogInterface dialog, int which) -> {
            mClickedDialogEntryIndex = which;
            sortType = mEntryValues[mClickedDialogEntryIndex].toString();
            //prefs.edit().putString(Constants.PREF_SORT_TYPE, sortType).apply();
            appPreferences.setPreference(Constants.PREF_SORT_TYPE, sortType);
            dialog.dismiss();
            activity.refreshCurrentActivity();
        });

       builder.create().show();
    }

    private int getValueIndex() {
        if (sortType != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(sortType)) {
                    return i;
                }
            }
        }
        return -1;
    }

}