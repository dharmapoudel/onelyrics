package com.lyricslover.onelyrics.helpers;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.main.MainActivity;
import com.lyricslover.onelyrics.main.PreferenceActivity;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.OfflineSongsUtil;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.services.DownloadService;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BottomNavigationViewHelper extends AppCompatActivity {

    public void addClick(BottomNavigationView bottomNavigationView, final MainActivity mainActivity) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        BottomNavigationItemView settings = (BottomNavigationItemView) menuView.getChildAt(2);
        BottomNavigationItemView download = (BottomNavigationItemView) menuView.getChildAt(1);
        BottomNavigationItemView addLyrics = (BottomNavigationItemView) menuView.getChildAt(0);


        download.setOnClickListener((v) -> {

            // progress dialog shown while we get a list of songs on the device
            ProgressDialog progressDialog = new ProgressDialog(mainActivity, R.style.OneLyricsDialogTheme);
            progressDialog.setMessage(mainActivity.getResources().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();

            // get the list of songs present on the device
            final ArrayList<Song> songArrayList = new ArrayList<>();
            OfflineSongsUtil.getOfflineSongs(mainActivity.getContentResolver(), songArrayList);

            progressDialog.dismiss();

            int songCount = songArrayList.size();
            // yes-no dialog before we start downloading the lyrics
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.OneLyricsDialogTheme);
            AlertDialog alertDialog = builder
                    .setMessage(mainActivity.getResources().getQuantityString(R.plurals.downloadPrompt, songCount, songCount))
                    .setPositiveButton(mainActivity.getString(R.string.yes), (DialogInterface dialog, int which) -> {
                        Intent intent = new Intent(mainActivity, DownloadService.class);
                        intent.putParcelableArrayListExtra(Constants.SONGS, songArrayList);
                        mainActivity.startService(intent);
                    })
                    .setNegativeButton(mainActivity.getString(R.string.no), (DialogInterface dialog, int which) -> dialog.dismiss())
                    .create();
            Objects.requireNonNull(alertDialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
            alertDialog.show();
        });

        //gitlab source code
        /*git.setOnClickListener((v) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.GITHUB_URL));
                mainActivity.startActivity(intent);
        });*/

        //add lyrics
        addLyrics.setOnClickListener(v -> {
            //mainActivity.startIdentifying();
            Toast.makeText(mainActivity.getApplicationContext(), mainActivity.getApplicationContext().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
        });


        //settings
        settings.setOnClickListener((v) -> {
            Intent intent = new Intent(mainActivity, PreferenceActivity.class);
            mainActivity.startActivity(intent);
        });
    }

}