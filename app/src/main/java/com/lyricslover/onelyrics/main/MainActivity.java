package com.lyricslover.onelyrics.main;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.acrcloud.rec.ACRCloudClient;
import com.acrcloud.rec.ACRCloudConfig;
import com.acrcloud.rec.ACRCloudResult;
import com.acrcloud.rec.IACRCloudListener;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.github.javiersantos.piracychecker.callbacks.PiracyCheckerCallback;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError;
import com.github.javiersantos.piracychecker.enums.PirateApp;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lyricslover.onelyrics.BuildConfig;
import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.appintro.AppIntroActivity;
import com.lyricslover.onelyrics.helpers.BottomNavigationViewHelper;
import com.lyricslover.onelyrics.helpers.PermissionHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.OfflineSongsUtil;
import com.lyricslover.onelyrics.misc.SortPreferenceDialog;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.receivers.MediaListener;
import com.lyricslover.onelyrics.services.LyricsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;


public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener,
        BillingProcessor.IBillingHandler, SearchView.OnQueryTextListener, LyricsViewAdapter.LyricsViewAdapterListener,
        IACRCloudListener {

    LyricsViewAdapter lyricsViewAdapter;
    View emptyListView;
    RecyclerView recyclerView;
    boolean isLyricsServiceStarted;
    boolean isMediaListenerServiceStarted;
    private final List<Song> songsList = new ArrayList<>();
    //private String selectedTheme;
    private BillingProcessor bp;
    private boolean proUser;
    //private MenuItem menuItem;
    private AppPreferences appPreferences;

    private String lastTheme;

    private ACRCloudClient acrCloudClient;
    private boolean initState = false;
    private boolean mProcessing = false;
    //private Long startTime = 0L;
    //private Long stopTime = 0L;
    private MenuItem searchMenuItem;

    private PulsatorLayout pulsatorLayout;
    private Animation pulseAnimation;

    private final String tag = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //initialize app preferences
        appPreferences = AppPreferences.getInstance(getApplicationContext());

        //set theme
        lastTheme = appPreferences.getSelectedTheme();
        setTheme(Utils.getCurrentTheme(lastTheme));

        //initialize billing
        bp = new BillingProcessor(this, Constants.LICENSE_KEY, this);
        bp.initialize();

        //show app intro
        showIntro();

        //show notice dialog
        showNotice();

        //inflate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start lyrics service
        //startLyricsService();
        //startMediaListener();

        //create .nomedia file
        if (PermissionHelper.allPermissionsAvailable(this)) {
            Utils.createNoMediaFile(getApplicationContext());
        }

        //display the offline lyrics list
        songsList.addAll(OfflineSongsUtil.getOfflineLyrics(getApplicationContext(), appPreferences));

        //setup recycler view
        lyricsViewAdapter = new LyricsViewAdapter(this, songsList, this);
        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(lyricsViewAdapter);
        recyclerView.setVisibility(!songsList.isEmpty() ? View.VISIBLE : View.GONE);

        //show empty view if songsList is empty
        emptyListView = findViewById(R.id.emptyListView);
        emptyListView.setVisibility(!songsList.isEmpty() ? View.GONE : View.VISIBLE);


        //if you want both swipe pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        //bottom navigation
        addBottomNavigation();

        //action bar is not null
        if (getSupportActionBar() != null) {

            //remove the shadow
            getSupportActionBar().setElevation(0);

            //change the font of the title
            TypefaceSpan typefaceSpan = new TypefaceSpan("roboto");
            SpannableString str = new SpannableString(getResources().getString(R.string.app_name));
            str.setSpan(typefaceSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(str);
        }

        //start anti piracy check
        try {
            startAntiPiracyCheck(getApplicationContext());
        } catch (Exception e) {
            Log.e(tag, "1Lyrics License exception occured " + e.getMessage());
        }

        //initialize ACR configs
        ACRCloudConfig acrCloudConfig = new ACRCloudConfig();
        acrCloudConfig.acrcloudListener = this;
        acrCloudConfig.context = this;
        acrCloudConfig.host = "identify-global.acrcloud.com";

        acrCloudConfig.accessKey = "xxxxxxxxxxxxxxxxx";//Access key goes here
        acrCloudConfig.accessSecret = "xxxxxxxxxxxxxxxx";//Access Secret goes here
        acrCloudConfig.recorderConfig.isVolumeCallback = false; // If you do not need volume callback, you set it false.

        acrCloudClient = new ACRCloudClient();
        initState = acrCloudClient.initWithConfig(acrCloudConfig);

        //ACR music recognition
        pulsatorLayout = findViewById(R.id.pulsator);

        pulsatorLayout.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        ImageView fabButton = findViewById(R.id.fabButton);
        fabButton.setOnClickListener(v -> {
            startIdentifying();
            pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
            fabButton.startAnimation(pulseAnimation);
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //add shortcut to open lyrics panel
        if (PermissionHelper.allPermissionsAvailable(this) && Utils.isO() && (proUser || appPreferences.getPreference(Constants.PRODUCT_ID, false))) {
            addDynamicShortcut();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangeReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //update theme if it's been updated
        String newTheme = appPreferences.getSelectedTheme();
        if (!lastTheme.contentEquals(newTheme)) {
            lastTheme = newTheme;
            setTheme(Utils.getCurrentTheme(newTheme));
            recreate();
        }

        if (PermissionHelper.allPermissionsAvailable(this)) {
            registerDownloadedActionReceiver();
            refreshCurrentActivity();

            startLyricsService();
            startMediaListener();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivity, menu);

        //search lyrics when icon is clicked
        searchMenuItem = menu.findItem(R.id.searchLyrics);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("search by title or artist");

        //remove search if development is not supported
        if (appPreferences.getPreference(Constants.PRODUCT_ID, false)) {
            searchMenuItem.setVisible(true);
            this.invalidateOptionsMenu();
        }

        //replace that ugly search icon
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setImageResource(R.drawable.search_icon);
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchMenuItem.collapseActionView();
            }
        });


        //hide the support icon if user has already supported
        MenuItem supportDevMenuItem = menu.findItem(R.id.menuSupportDev);
        if (proUser || appPreferences.getPreference(Constants.PRODUCT_ID, false)) {
            supportDevMenuItem.setVisible(false);
            this.invalidateOptionsMenu();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.menuSettings:
                startActivity(new Intent(this, PreferenceActivity.class));
                break;*/

            case R.id.menuSort:
                new SortPreferenceDialog(this, appPreferences).createDialog(this);
                break;

            case R.id.menuSupportDev:
                if (BillingProcessor.isIabServiceAvailable(this) && bp.isOneTimePurchaseSupported()) {
                    bp.purchase(this, Constants.PRODUCT_ID, null);
                }
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        //Toast.makeText(getApplicationContext(), "History Restored!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.next_time), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        //Toast.makeText(getApplicationContext(), "Billing initialized!", Toast.LENGTH_SHORT).show();
        proUser = bp.isPurchased(Constants.PRODUCT_ID) && bp.loadOwnedPurchasesFromGoogle();
        proUser = (BuildConfig.DEBUG) || proUser;
        appPreferences.setPreference(Constants.PRODUCT_ID, proUser);
        if (!proUser) setFreeMode();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


    /*@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(Constants.PREF_SORT_TYPE.equalsIgnoreCase(key)){
            refreshCurrentActivity();
        }
    }*/

    public void onSortReverseClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (view.getId() == R.id.sortOrder) {
            appPreferences.setPreference(Constants.PREF_SORT_ORDER, checked);
            refreshCurrentActivity();
        }

    }

    private void showIntro() {
        boolean introS = appPreferences.getPreference(Constants.PREF_APP_INTRO, false);
        if (!(PermissionHelper.allPermissionsAvailable(MainActivity.this) && introS)) {
            appPreferences.setPreference(Constants.PREF_APP_INTRO, true);
            new Handler().postDelayed(() -> startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), Constants.APP_INTRO_REQUEST), 50);
        }
    }

    private void showNotice() {
        if (!appPreferences.getPreference(Constants.PREF_APP_NOTICE, false)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.OneLyricsDialogTheme);
            LayoutInflater inflater = this.getLayoutInflater();
            dialogBuilder.setCancelable(false);
            View dialogView = inflater.inflate(R.layout.notice_dialog, null);
            dialogBuilder.setView(dialogView);

            AlertDialog alertDialog = dialogBuilder.create();
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                alertDialog.show();
            }

            dialogView.findViewById(R.id.understood).setOnClickListener((View v) -> alertDialog.dismiss());

            appPreferences.setPreference(Constants.PREF_APP_NOTICE, true);
        }
    }


    private final BroadcastReceiver dataChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (PermissionHelper.allPermissionsAvailable(MainActivity.this)) {
                refreshCurrentActivity();
            }
        }
    };


    public void refreshCurrentActivity() {
        songsList.clear();
        songsList.addAll(OfflineSongsUtil.getOfflineLyrics(getApplicationContext(), appPreferences));
        lyricsViewAdapter.updateItems(songsList);
        //lyricsViewAdapter.getFilter().filter("");
        updateViews();
    }

    public void updateViews() {
        emptyListView.setVisibility(!songsList.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(!songsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    //bottom navigation
    private void addBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper bottomNavViewHelper = new BottomNavigationViewHelper();
        bottomNavViewHelper.addClick(bottomNavigation, MainActivity.this);
        bottomNavigation.setItemIconTintList(null);
        bottomNavigation.clearFocus();
    }

    private void startLyricsService() {
        if (PermissionHelper.allPermissionsAvailable(this) && !isLyricsServiceStarted) {
            Intent intent = new Intent(this, LyricsService.class);
            ContextCompat.startForegroundService(this, intent);
            isLyricsServiceStarted = true;
        }
    }

    private void startMediaListener() {
        if (PermissionHelper.allPermissionsAvailable(this) && !isMediaListenerServiceStarted) {
            Intent intent = new Intent(this, MediaListener.class);
            ContextCompat.startForegroundService(this, intent);
            isMediaListenerServiceStarted = true;
        }
    }

    private void registerDownloadedActionReceiver() {
        IntentFilter iF = new IntentFilter();
        iF.addAction(Constants.ACTION_LYRICS_DOWNLOADED);
        iF.addAction(Constants.ACTION_ALBUMART_DOWNLOADED);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangeReceiver, iF);
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof LyricsViewAdapter.ViewHolder && viewHolder.getAdapterPosition() >= 0) {
            // get the lyrics to be removed
            Song song = songsList.get(viewHolder.getAdapterPosition());

            // remove the item from recycler view
            lyricsViewAdapter.removeItem(viewHolder.getAdapterPosition());

            //update view and show no lyrics available message
            updateViews();

            //delete the lyrics file
            Utils.deleteLyricsFile(song, getApplicationContext());

            // show toast
            Toast.makeText(getApplicationContext(), Utils.getSongTitle(song) + " deleted", Toast.LENGTH_SHORT).show();

        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void addDynamicShortcut() {
        Context context = getApplicationContext();

        ShortcutManager mShortcutManager = context.getSystemService(ShortcutManager.class);

        Intent showLyricsPanelIntent = new Intent(Constants.SHOWLYRICSPANEL);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "openlyricspanel")
                .setShortLabel("Open Panel")
                .setLongLabel("Open lyrics panel")
                .setIcon(Icon.createWithResource(context, R.drawable.panel_icon))
                .setIntent(showLyricsPanelIntent)
                .build();

        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
        shortcutInfoList.add(shortcut);

        if (mShortcutManager != null) {
            mShortcutManager.setDynamicShortcuts(shortcutInfoList);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        //lyricsViewAdapter.getFilter().filter(s.toLowerCase());
        String searchText = s.toLowerCase();
        List<Song> filteredList = new ArrayList<>();
        for (Song song : songsList) {
            if (song.getArtist().toLowerCase().contains(searchText) || song.getTrack().toLowerCase().contains(searchText)) {
                filteredList.add(song);
            }
        }
        lyricsViewAdapter.updateItems(filteredList);
        emptyListView.setVisibility(!filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(!filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        return true;
    }

    @Override
    public void onLyricsViewItemSelected(Song song) {
        // Toast.makeText(getApplicationContext(), "Selected: " + song.getTrack() + ", " + song.getArtist(), Toast.LENGTH_LONG).show();
    }


    private void startAntiPiracyCheck(Context context) {
        //Preferences pref = new Preferences(getApplicationContext());

        PiracyChecker piracyChecker = new PiracyChecker(context);
        piracyChecker.enableInstallerId(InstallerID.GOOGLE_PLAY);
        piracyChecker.enableDebugCheck(true);
        //piracyChecker.enableUnauthorizedAppsCheck(true);
        piracyChecker.enableGooglePlayLicensing(Constants.LICENSE_KEY);
        piracyChecker.callback(new PiracyCheckerCallback() {
            @Override
            public void doNotAllow(@NonNull PiracyCheckerError piracyCheckerError, @Nullable PirateApp pirateApp) {
                Log.e(tag, "1Lyrics license verification failed: " + piracyCheckerError.toString());
                Toast.makeText(context, "License verification failed: running free version of the app.", Toast.LENGTH_SHORT).show();
                //set to free mode
                setFreeMode();
            }

            @Override
            public void allow() {
                Log.e(tag, "1Lyrics license verification passed.");
                if (BuildConfig.DEBUG) {
                    Toast.makeText(context, "License verification passed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!BuildConfig.DEBUG) piracyChecker.start();
    }

    private void setFreeMode() {
        //update the key
        appPreferences.setPreference(Constants.PRODUCT_ID, false);

        //also hide search key
        if (searchMenuItem != null) {
            searchMenuItem.setVisible(false);
        }
        //update the theme to default
        appPreferences.setPreference(Constants.PREF_APP_THEME, "1");

    }

    @Override
    public void onResult(ACRCloudResult acrCloudResult) {
        stopIdentifying();

        String result = acrCloudResult.getResult();

        String titleArtist = handleResult(result);
        if (!titleArtist.equalsIgnoreCase("Couldn't detect song!")) {
            Toast.makeText(getApplicationContext(), "Fetching lyrics ...", Toast.LENGTH_SHORT).show();
            String[] songInfo = handleResult(result).split("-");
            String songName = songInfo[0];
            String artistName = songInfo[1];
            long playOffsetMs = Long.parseLong(songInfo[2]);

            //send an intent to LyricsService
            Intent intent1 = new Intent(getApplicationContext(), LyricsService.class);
            intent1.putExtra(Constants.IS_MUSIC_PLAYING, false);
            intent1.putExtra(Constants.OFFLINE_READ, true);
            intent1.putExtra(Constants.ARTIST, artistName);
            intent1.putExtra(Constants.TRACK, songName);
            intent1.putExtra(Constants.POSITION, playOffsetMs);
            intent1.putExtra(Constants.TIME_ELAPSED, System.currentTimeMillis());
            intent1.putExtra(Constants.OPEN_LYRICS_PANEL, true);
            getApplicationContext().startService(intent1);
        }

        //if (song_name != "{\"status\":{\"msg\":\"No result\",\"code\":1001,\"version\":\"1.0\"}}")
        // startTime = System.currentTimeMillis();
    }


    @Override
    public void onVolumeChanged(double v) {
        //float time = (System.currentTimeMillis() - startTime) / 1000;
    }

    //Members of IACRCloudlistener
    public void startIdentifying() {
        if (!this.initState) {
            Toast.makeText(getApplicationContext(), "init error", Toast.LENGTH_SHORT).show();
        }

        PermissionHelper.verifyAudioPermissions(this);
        if (!mProcessing) {
            pulsatorLayout.start();
            //mResult?.text = "";
            //songname.text = "";
            //acrlyrics.text = "start error!"
            mProcessing = acrCloudClient != null && acrCloudClient.startRecognize();
            /*if(acrCloudClient == null || !acrCloudClient.startRecognize()) {
                mProcessing = false;
                //acrlyrics.text = "start error!"
            }*/
            //startTime = System.currentTimeMillis();
        }
    }

    private void stopIdentifying() {
        if (mProcessing && acrCloudClient != null) {
            pulsatorLayout.stop();
            pulseAnimation.cancel();
            acrCloudClient.cancel();
        }
        mProcessing = false;
    }

    private String handleResult(String acrResult) {
        String res = "Couldn't detect song!";

        try {
            JSONObject json = new JSONObject(acrResult);
            JSONObject status = json.getJSONObject("status");
            int code = status.getInt("code");
            if (code == 0) {
                JSONObject metadata = json.getJSONObject("metadata");
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    JSONObject tt = (JSONObject) musics.get(0);
                    String title = tt.getString("title");

                    JSONArray artistt = tt.getJSONArray("artists");
                    JSONObject art = (JSONObject) artistt.get(0);
                    String artist = art.getString("name");

                    long playOffsetMs = tt.getLong("play_offset_ms");

                    res = title + "-" + artist + "-" + playOffsetMs;
                    Toast.makeText(getApplicationContext(), "Detected song " + title + " by " + artist, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e("ACR", "JSONException", e);
        }
        return res;
    }
}


