package com.lyricslover.onelyrics.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.lrcview.LrcView;
import com.lyricslover.onelyrics.lyricsproviders.LocalStorageLyricsProvider;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Lyrics;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.receivers.OnSwipeTouchListener;
import com.lyricslover.onelyrics.tasks.LrcRunnable;

import org.jsoup.internal.StringUtil;

import androidx.core.content.ContextCompat;

public class LyricsPanelHelper {

    private final Context context;
    private final WindowManager windowManager;
    private final AppPreferences appPreferences;

    private Thread lrcThread;

    private LinearLayout panelLayout;

    private Handler panelControlsHandler;
    private LrcRunnable lrcRunnable;

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    protected static final String TAG = TriggerHelper.class.getSimpleName();

    //private static final String TAG = LyricsPanelHelper.class.getSimpleName();
    @SuppressLint("InlinedApi")
    private static final int LAYOUT_FLAG = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_PHONE : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

    public LyricsPanelHelper(Context s) {
        this.context = s;
        this.appPreferences = AppPreferences.getInstance(s.getApplicationContext());
        this.windowManager = appPreferences.getWindowManager();
        this.panelLayout = getLyricsPanel();
    }


    public LinearLayout getLyricsPanel() {
        if (panelLayout == null) {
            //panelLayout = new LinearLayout(context);
            panelLayout = (LinearLayout) appPreferences.getLayoutInflater().inflate(R.layout.lyrics_sheet, null);
            //lyricsPanel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ProgressBar progressBar = panelLayout.findViewById(R.id.progressbar);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                progressBar.getIndeterminateDrawable().setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), BlendMode.SRC_IN));
            } else {
                progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }
            //panelLayout.addView(lyricsPanel);
        }

        return panelLayout;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachPanelTouchListeners() {
        // swipe listener to dismiss the lyrics panel
        View lyricsPanel = panelLayout.getChildAt(0);

        if (appPreferences.getPreference(Constants.PRODUCT_ID, false)) {
            lyricsPanel.setOnTouchListener(new OnSwipeTouchListener(panelLayout, getLyricsPanelParams(), windowManager, appPreferences) {
                @Override
                public void onTouch() {
                    showPanelControls(panelLayout);
                }
            });
        }

        panelLayout.findViewById(R.id.lyricsScrollView).setOnTouchListener((v, event) -> {
            showPanelControls(panelLayout);
            return false;
        });

        panelLayout.findViewById(R.id.panel_close_button).setOnTouchListener((v, event) -> {
            closeLyricsPanel();
            return false;
        });

    }

    public void resetLyricsPanel(Context context, Song song) {
        //reset title to empty or song title
        TextView titleTV = panelLayout.findViewById(R.id.title);
        titleTV.setVisibility(appPreferences.getPreference(Constants.PANEL_HEAD, true) ? View.GONE : View.VISIBLE);
        titleTV.setText(song != null ? Utils.getSongTitle(song) : context.getResources().getString(R.string.empty));

        //reset lyrics view
        TextView lyricsTV = panelLayout.findViewById(R.id.lyricsText);
        lyricsTV.setVisibility(View.VISIBLE);
        lyricsTV.setText(context.getResources().getString(R.string.fetchingLyrics));

        //reset lrc view
        final LrcView lrcView = panelLayout.findViewById(R.id.lyricsLrc);
        lrcView.setLrc(context.getResources().getString(R.string.empty));
        lrcView.setVisibility(View.GONE);

        //also reset the runnable
        if (lrcRunnable != null) lrcRunnable.setThreadCancelled(true);

        // reset lyrics scroll view, scroll to top
        panelLayout.findViewById(R.id.lyricsScrollView).scrollTo(0, 0);

        //show the progress bar
        panelLayout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
    }

    public void showLyricsPanel() {
        View lyricsPanel = panelLayout.getChildAt(0);
        TextView titleView = lyricsPanel.findViewById(R.id.title);

        //set panel title to empty or song title
        titleView.setVisibility(appPreferences.getPreference(Constants.PANEL_HEAD, true) ? View.GONE : View.VISIBLE);

        //add some blur to the lyrics panel
        //if(song != null) blurLyricsPanel(song);

        //slide up animation
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        lyricsPanel.startAnimation(animation);
    }

    public void attachLyricsPanel() {

        //attach the panel layout view to window
        windowManager.addView(panelLayout, getLyricsPanelParams());

        //add touch listeners to lyrics panel
        attachPanelTouchListeners();
    }

    public void attachAndShowLyricsPanel() {
        //remove panel
        removeLyricsPanel();

        //attach to windowManager, add listeners
        attachLyricsPanel();

        //blur, animate to show
        showLyricsPanel();
    }

    public void removeLyricsPanel() {
        try {
            if (panelLayout != null && panelLayout.getParent() != null) {
                panelLayout.getChildAt(0).setTranslationY(0);
                windowManager.removeView(panelLayout);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Exception occured while removing trigger" + e.getLocalizedMessage());
        }
    }

    public void closeLyricsPanel() {
        if (panelLayout != null && panelLayout.getParent() != null) {
            final View lyricsPanel = panelLayout.getChildAt(0);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
            lyricsPanel.startAnimation(animation);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeLyricsPanel();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
        }
    }

    public WindowManager.LayoutParams getLyricsPanelParams() {
        // params for the lyrics panel
        WindowManager.LayoutParams lyricsPanelParams = new WindowManager.LayoutParams(
                //TO DO: WindowManager was used instead of ViewGroup
                ViewGroup.LayoutParams.MATCH_PARENT,
                appPreferences.getPanelHeight(),
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.RGBA_8888);


        //force screen on if lyrics panel is displayed
        boolean forceScreenOn = appPreferences.getPreference(Constants.PREF_FORCE_SCREEN_ON, false);
        if (forceScreenOn) {
            lyricsPanelParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }

        lyricsPanelParams.gravity = Gravity.BOTTOM;
        lyricsPanelParams.x = 0;
        lyricsPanelParams.y = appPreferences.getPreference(Constants.PANEL_Y, 0); //  + dpToPX(appPreferences.getGetDisplayMetrics(), 60) ;
        // appPreferences.getPanelY(); //-168;

        return lyricsPanelParams;
    }

    /*public int dpToPX(DisplayMetrics displayMetrics, float dp) {
        return   (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }*/

    public void updateLyricsPanel(int height) {
        WindowManager.LayoutParams lyricsPanelParams = getLyricsPanelParams();
        lyricsPanelParams.height = height;
        if(panelLayout != null && panelLayout.getParent() != null) {
            windowManager.updateViewLayout(panelLayout, lyricsPanelParams);
        }
    }

    public void blurLyricsPanel(Song song) {

        View lyricsPanel = panelLayout.findViewById(R.id.lyricsPanel);
        if (appPreferences.getPreference(Constants.PANEL_BLUR, false) && song != null) {
            Bitmap image = (new BlurHelper(context)).blur(Utils.getAlbumArtBitmap(song, context));
            lyricsPanel.setBackground(new BitmapDrawable(context.getResources(), image));
        } else {
            lyricsPanel.setBackgroundColor(ContextCompat.getColor(context, R.color.listDivider_dark));
        }
        lyricsPanel.getBackground().setAlpha((int) (appPreferences.getPanelAlpha() * 2.55));
    }

    public void updateLyrics(Song song, Intent intent) {

        //fetch lyrics from local storage
        Lyrics lyrics = (new LocalStorageLyricsProvider()).provideLyrics(song, context);

        //LinearLayout lyricsPanel = getLyricsPanel();
        TextView lyricsTextView = panelLayout.findViewById(R.id.lyricsText);
        LrcView lrcView = panelLayout.findViewById(R.id.lyricsLrc);

        //update lyrics panel title
        TextView titleTV = panelLayout.findViewById(R.id.title);
        titleTV.setText(song != null ? Utils.getSongTitle(song) : context.getResources().getString(R.string.empty));

        //NestedScrollView lyricsScrollView = lyricsPanel.findViewById(R.id.lyricsNoticeScrollView);
        final ProgressBar progressBar = panelLayout.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        //● get lrcRunnable
        //if (lrcRunnable == null)
        //   lrcRunnable = LrcRunnable.getInstance(isMusicPlaying, lrcView, 0L, context);

        //lyrics not found is visible by default
        lyricsTextView.setText(context.getResources().getString(R.string.noLyricsFound));
        lyricsTextView.setVisibility(View.VISIBLE);

        if (lyrics != null && !StringUtil.isBlank(lyrics.getText())) {
            //boolean trackChanged = intent.getBooleanExtra(Constants.TRACK_CHANGED, false);
            //now that music is playing and lyrics is available, show the trigger handle
                /*if (isMusicPlaying) {
                    service.updateTrigger(appPreferences.getTriggerWidth());
                }*/
            //panelLayout.findViewById(R.id.lyricsScrollView).fullScroll(ScrollView.FOCUS_UP);

            //txt format lyrics
            if (lyrics.getType().equalsIgnoreCase(Constants.FILE_TYPE_TXT)) {
                lrcView.setVisibility(View.GONE);
                lyricsTextView.setText(lyrics.getText());
                lyricsTextView.setVisibility(View.VISIBLE);
            }

            //lrc format lyrics
            if (lyrics.getType().equalsIgnoreCase(Constants.FILE_TYPE_LRC)) {
                lyricsTextView.setVisibility(View.GONE);
                lrcView.setLrc(lyrics.getText());
                lrcView.setVisibility(View.VISIBLE);
                updateLRCLyricsView(intent, lrcView);
            }
        }
    }

    public void updateLRCLyricsView(Intent intent, LrcView lrcView){
        //boolean trackChanged = intent.getBooleanExtra(Constants.TRACK_CHANGED, false);
        boolean offlineRead = intent.getBooleanExtra(Constants.OFFLINE_READ, false);
        long timeElapsed = System.currentTimeMillis() - intent.getLongExtra(Constants.TIME_ELAPSED, 0L);
        final long position = intent.getLongExtra(Constants.POSITION, 0L) + timeElapsed + 300;

        if (!offlineRead) {
            final boolean isMusicPlaying = intent.getBooleanExtra(Constants.ISMUSICPLAYING, false);

            if (lrcRunnable != null) {
                lrcRunnable.setThreadCancelled(true);
                lrcRunnable = null;
            }

            lrcRunnable = LrcRunnable.getInstance(isMusicPlaying, lrcView, position, context);

            Thread lrcThread1 = getLrcThread(lrcRunnable);
            if (lrcThread1 == null || !lrcThread1.isAlive()) {
                Thread t = new Thread(lrcRunnable, Constants.LYRICS_SYNC_THREAD);
                setLrcThread(t);
                t.start();
            }

            lrcRunnable.setPosition(position);
            lrcRunnable.setThreadCancelled(!isMusicPlaying);
        }
    }

    public void registerOnSharedPreferenceChangeListener() {
        sharedPreferenceChangeListener = (SharedPreferences sharedPreferences, String key) -> {
            switch (key) {
                case Constants.PANEL_HEIGHT:
                    updateLyricsPanel(appPreferences.getPanelHeight());
                    break;

                case Constants.PANEL_ALPHA:
                    panelLayout.findViewById(R.id.lyricsPanel).getBackground().setAlpha((int) (appPreferences.getPanelAlpha() * 2.55));
                    break;

                case Constants.PANEL_HEAD:
                    panelLayout.findViewById(R.id.title).setVisibility(appPreferences.getPreference(Constants.PANEL_HEAD, true) ? View.GONE : View.VISIBLE);
                    break;

                case Constants.PANEL_BLUR:
                    blurLyricsPanel(appPreferences.getSongFromPreferences());
                    break;

                default:
                    //
                    break;
            }
        };
        appPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private void showPanelControls(final LinearLayout panelLayout) {
        panelLayout.findViewById(R.id.panel_close_button).setVisibility(View.VISIBLE);
        panelLayout.findViewById(R.id.topDragBar).setVisibility(View.VISIBLE);

        //hide them after 3 seconds
        Runnable panelControlsRunnable = () -> {
            panelLayout.findViewById(R.id.panel_close_button).setVisibility(View.GONE);
            panelLayout.findViewById(R.id.topDragBar).setVisibility(View.INVISIBLE);
        };

        panelControlsHandler = (panelControlsHandler != null) ? panelControlsHandler : new Handler();
        panelControlsHandler.removeCallbacksAndMessages(null);
        panelControlsHandler.postDelayed(panelControlsRunnable, 3000);
    }

    public Thread getLrcThread(LrcRunnable runnable) {
        if (lrcThread == null || !lrcThread.isAlive()) {
            lrcThread = new Thread(runnable, Constants.LYRICS_SYNC_THREAD);
        }
        return lrcThread;
    }

    public void setLrcThread(Thread lrcThread) {
        this.lrcThread = lrcThread;
    }

}