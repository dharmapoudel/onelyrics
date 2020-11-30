package com.lyricslover.onelyrics.main;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;

import androidx.appcompat.app.AppCompatActivity;


public class FaqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //initialize app preferences
        AppPreferences appPreferences = new AppPreferences(getApplicationContext());

        //set theme
        String selectedTheme = appPreferences.getPreference(Constants.PREF_APP_THEME, "1");
        setTheme(Utils.getCurrentTheme(selectedTheme));

        //inflate the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        //remove the shadow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        //change the font of the title
        TypefaceSpan typefaceSpan = new TypefaceSpan("roboto");
        SpannableString str = new SpannableString(getResources().getString(R.string.faq_title));
        str.setSpan(typefaceSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(str);

        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }*/

    /*@Override
    protected void onRestart() {
        super.onRestart();
    }*/

    /*@Override
    protected void onPause() {
        super.onPause();
    }*/

    /*@Override
    protected void onResume() {
        super.onResume();
    }*/

    /*@Override
    public void onContentChanged() {
        super.onContentChanged();
    }*/


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    /*@Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }*/

}


