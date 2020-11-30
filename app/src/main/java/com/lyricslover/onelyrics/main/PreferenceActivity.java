package com.lyricslover.onelyrics.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.appintro.AppIntroActivity;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.ThemePreferenceDialog;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.services.LyricsService;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;


public class PreferenceActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;
    MyPreferenceFragment preferenceFragment;
    private AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //initialize app preferences
        appPreferences = AppPreferences.getInstance(getApplicationContext());

        //String selectedTheme = appPreferences.getPreferenceStringByKey(Constants.PREF_APP_THEME, "1");
        setTheme(Utils.getCurrentTheme(appPreferences.getSelectedTheme()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        //initialize billing
        bp = new BillingProcessor(this, Constants.LICENSE_KEY, this);
        bp.initialize();

        //set preferences
        preferenceFragment = new MyPreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();


        //change the font of the title

        if (getSupportActionBar() != null) {
            TypefaceSpan typefaceSpan = new TypefaceSpan("roboto");
            SpannableString str = new SpannableString(getResources().getString(R.string.menu_settings));
            str.setSpan(typefaceSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(str);
            //set as bac
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //remove the shadow
            getSupportActionBar().setElevation(0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preferenceactivity, menu);
        if (appPreferences.getPreference(Constants.PRODUCT_ID, false)) {
            MenuItem item = menu.findItem(R.id.menuSupportDev);
            item.setVisible(false);
            this.invalidateOptionsMenu();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menuSupportDev:
            default:
                purchaseProduct();

        }

        return super.onOptionsItemSelected(item);
    }

    private void purchaseProduct() {
        boolean isBillingProcessorAvailable = BillingProcessor.isIabServiceAvailable(this);
        boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
        if (isBillingProcessorAvailable && isOneTimePurchaseSupported) {
            bp.purchase(this, Constants.PRODUCT_ID, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
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
    }


    public static class MyPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private AppPreferences pref;

        @SuppressLint("DefaultLocale")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //super.onCreate(savedInstanceState);
            setPreferencesFromResource(R.xml.preferences, rootKey);

            //initialize preferences
            pref = AppPreferences.getInstance(getContext());

            SeekBarPreference triggerOffset = findPreference(Constants.TRIGGER_OFFSET);
            SeekBarPreference triggerWidth = findPreference(Constants.TRIGGER_WIDTH);
            SeekBarPreference triggerHeight = findPreference(Constants.TRIGGER_HEIGHT);
            SeekBarPreference triggerAlpha = findPreference(Constants.TRIGGER_ALPHA);
            SeekBarPreference panelHeight = findPreference(Constants.PANEL_HEIGHT);
            SeekBarPreference panelAlpha = findPreference(Constants.PANEL_ALPHA);

            ListPreference triggerPosition = findPreference(Constants.TRIGGER_POS);
            ListPreference swipeDirection = findPreference(Constants.SWIPE_DIRECTION);

            setSummary(triggerOffset, pref.getPreference(Constants.TRIGGER_OFFSET, 52));
            setSummary(triggerWidth, pref.getPreference(Constants.TRIGGER_WIDTH, 0));
            setSummary(triggerHeight, pref.getPreference(Constants.TRIGGER_HEIGHT, 82));
            setSummary(triggerAlpha, pref.getTriggerAlpha());

            setSummary(panelHeight, pref.getPreference(Constants.PANEL_HEIGHT, 26));
            setSummary(panelAlpha, pref.getPanelAlpha());

            if (triggerOffset != null)
                triggerOffset.setOnPreferenceChangeListener(this::onPreferenceChange);
            if (triggerWidth != null)
                triggerWidth.setOnPreferenceChangeListener(this::onPreferenceChange);
            if (triggerHeight != null)
                triggerHeight.setOnPreferenceChangeListener(this::onPreferenceChange);
            if (triggerAlpha != null)
                triggerAlpha.setOnPreferenceChangeListener(this::onPreferenceChange);

            if (panelHeight != null)
                panelHeight.setOnPreferenceChangeListener(this::onPreferenceChange);
            if (panelAlpha != null)
                panelAlpha.setOnPreferenceChangeListener(this::onPreferenceChange);

            if (triggerPosition != null)
                triggerPosition.setOnPreferenceChangeListener(this::onTriggerPositionPreferenceChange);
            if (swipeDirection != null)
                swipeDirection.setOnPreferenceChangeListener(this::onSwipeDirectionPreferenceChange);

        }

        private boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference != null) {
                final int progress = Integer.parseInt(String.valueOf(newValue));
                pref.setPreference(preference.getKey(), progress);
                setSummary(preference, progress);
            }
            return true;
        }

        private boolean onTriggerPositionPreferenceChange(Preference preference, Object newValue) {
            if (preference != null) {
                final int progress = Integer.parseInt(String.valueOf(newValue));
                pref.setPreference(preference.getKey(), progress);
                preference.setSummary(getResources().getStringArray(R.array.triggerPos)[progress - 1]);
            }
            return true;
        }

        private boolean onSwipeDirectionPreferenceChange(Preference preference, Object newValue) {
            if (preference != null) {
                final int progress = Integer.parseInt(String.valueOf(newValue));
                pref.setPreference(preference.getKey(), progress);
                preference.setSummary(getResources().getStringArray(R.array.triggerSwipeDirection)[progress - 1]);
            }
            return true;
        }

        @SuppressLint("DefaultLocale")
        private void setSummary(Preference preference, int progress) {
            if (preference != null) {
                preference.setSummary(String.format("%d", progress));
            }
        }

        @Override
        public void onResume() {
            pref.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Constants.PREF_APP_THEME.equalsIgnoreCase(key) && getActivity() != null) {
                getActivity().recreate();
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Context context = preference.getContext();
            switch (preference.getKey()) {
                case Constants.PREF_AUTO_CLOSE_PANEL:
                    pref.setPreference(Constants.PREF_AUTO_CLOSE_PANEL, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.PREF_APP_INTRO:
                    startActivityForResult(new Intent(context, AppIntroActivity.class), Constants.APP_INTRO_REQUEST);
                    break;

                case Constants.PREF_APP_THEME:
                    if (!pref.getPreference(Constants.PRODUCT_ID, false)) {
                        Toast.makeText(context, context.getResources().getString(R.string.donate_only), Toast.LENGTH_SHORT).show();
                    } else {
                        new ThemePreferenceDialog(context, pref).createDialog();
                    }
                    break;

                case Constants.PREF_EXCLUDE_APPS:
                    if (pref.getPreference(Constants.PRODUCT_ID, false)) {
                        showAppSelectDialog(context);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.donate_only), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case Constants.PREF_APP_FAQ:
                    startActivity(new Intent(context, FaqActivity.class));
                    break;

                case Constants.PANEL_HEAD:
                    pref.setPreference(Constants.PANEL_HEAD, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.CACHE_LYRICS:
                    pref.setPreference(Constants.CACHE_LYRICS, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.CACHE_ALBUMART:
                    pref.setPreference(Constants.CACHE_ALBUMART, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.HIGHLIGHT_OFFLINE_SONG_LYRICS:
                    pref.setPreference(Constants.HIGHLIGHT_OFFLINE_SONG_LYRICS, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.PREF_FORCE_SCREEN_ON:
                    pref.setPreference(Constants.PREF_FORCE_SCREEN_ON, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.PREF_CLOSE_ON_BACK_PRESS:
                    pref.setPreference(Constants.PREF_CLOSE_ON_BACK_PRESS, ((SwitchPreference) preference).isChecked());
                    context.stopService(new Intent(context, LyricsService.class));
                    break;

                case Constants.TRIGGER_VIBRATION:
                    pref.setPreference(Constants.TRIGGER_VIBRATION, ((SwitchPreference) preference).isChecked());
                    break;

                case Constants.PANEL_BLUR:
                    pref.setPreference(Constants.PANEL_BLUR, ((SwitchPreference) preference).isChecked());
                    break;

                default:
                    //do nothing
                    break;
            }

            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onPause() {
            pref.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        public void showAppSelectDialog(Context context) {

            // progress dialog shown while we get a list of apps on the device
            //ProgressBar progressDialog = new ProgressBar(context); //, R.style.OneLyricsDialogTheme);
            //progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
            //progressDialog.setCancelable(false);
            //progressDialog.show();

            Map<String, String> applicationMap = Utils.getInstalledAppList(context);
            CharSequence[] installedApps = applicationMap.keySet().toArray(new CharSequence[0]);

            //progressDialog.dismiss();

            //get boolean array of checked apps
            String excludedApps = pref.getPreference(Constants.PREF_EXCLUDE_APPS, "");
            boolean[] checkedApps = Utils.getValueIndexes(installedApps, Utils.getSelectedAppsList(excludedApps));


            // yes-no dialog before we start downloading the lyrics
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.OneLyricsDialogTheme);
            builder.setTitle(context.getResources().getString(R.string.exclude_apps_description))
                    .setMultiChoiceItems(installedApps, checkedApps, (DialogInterface dialog, int which, boolean isChecked) -> {
                        //get the clicked item
                        String appName = (applicationMap.keySet().toArray()[which]).toString();

                        //update the preferences
                        List<String> selectedAppsList = Utils.getSelectedAppsList(excludedApps);

                        //add/remove app and then update preferences
                        if (!selectedAppsList.contains(appName) && isChecked) {
                            selectedAppsList.add(appName);
                            pref.setPreference(Constants.PREF_EXCLUDE_APPS, selectedAppsList.toString());
                        } else if (selectedAppsList.contains(appName) && !isChecked) {
                            selectedAppsList.remove(appName);
                            pref.setPreference(Constants.PREF_EXCLUDE_APPS, selectedAppsList.toString());
                        }
                    })
                    .setPositiveButton(context.getString(R.string.ok), (DialogInterface dialog, int which) -> dialog.dismiss());

            builder.create().show();
        }
    }

}