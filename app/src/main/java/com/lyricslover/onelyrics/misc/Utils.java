package com.lyricslover.onelyrics.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.pojos.Song;

import org.jsoup.internal.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    private Utils() {
    }

    protected static final String TAG = Utils.class.getSimpleName();


    static String getLyricsDirectoryPath(Context context) {
        return isP()
                ? getOneLyricsStorageDir(context).getAbsolutePath() + File.separator
                : context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + File.separator + Constants.APP_BASE_PATH + File.separator;
    }

    private static File getOneLyricsStorageDir(Context context) {
        File externalFilesDir = new File(context.getExternalFilesDir(null), Constants.APP_BASE_PATH);
        if (!externalFilesDir.mkdirs()) {
            //Log.e(TAG, "Directory not created");
        }
        return externalFilesDir;
    }

    public static File getLyricsDirectory(Context context) {
        return isP() ? getOneLyricsStorageDir(context) : new File(getLyricsDirectoryPath(context));
        //return new File(getLyricsDirectoryPath());
    }

    private static String getFileNameWithoutExtension(String track, String artist) {
        String artistU = replaceChars(artist);
        String trackU = replaceChars(track);
        return trackU + "-" + artistU;
    }

    public static String getSongTitle(Song song) {
        return song.getArtist() + " - " + song.getTrack();
    }

    public static String replaceChars(String string) {
        return (!"".equalsIgnoreCase(string)) ? string.replace(" ", "_")
                .replace("-", "_")
                //.replaceAll(",", "")
                //.replaceAll("\\(", "").replaceAll("\\)", "")
                : "";
    }

    public static Song getSongFromIntent(Intent intent) {
        String artist = intent.getStringExtra(Constants.ARTIST);
        String track = intent.getStringExtra(Constants.TRACK);
        long position = intent.getLongExtra(Constants.POSITION, 0);
        long duration = intent.getLongExtra(Constants.DURATION, 0);

        Song song = null;
        if (!StringUtil.isBlank(artist) && !StringUtil.isBlank(track)) {
            song = new Song(track, artist, position, duration);
        }
        return song;
    }

    public static Drawable getAlbumArtDrawable(Song song, Context context) {
        String pngFileName = Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_PNG);
        String jpgFileName = Utils.getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_JPG);
        Drawable drawable = null;

        if (fileExists(pngFileName, context)) {
            drawable = Drawable.createFromPath(getLyricsDirectoryPath(context) + pngFileName);
        } else if (fileExists(jpgFileName, context)) {
            drawable = Drawable.createFromPath(getLyricsDirectoryPath(context) + jpgFileName);
        }

        return drawable;
    }

    public static Bitmap getAlbumArtBitmap(Song song, Context context) {
        Drawable drawable = getAlbumArtDrawable(song, context);
        Bitmap bitmap;
        if (drawable != null) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(0xCC444444);
        }
        return bitmap;
    }

    public static String getAlbumArtPathFromSong(Song song, Context context) {
        String fileName = "";
        String fileNameWithoutExtension = getFileNameWithoutExtension(replaceChars(song.getTrack()), replaceChars(song.getArtist()));
        String pngFile = fileNameWithoutExtension + ".png";
        String jpgFile = fileNameWithoutExtension + ".jpg";
        if (fileExists(pngFile, context)) {
            fileName = getLyricsDirectoryPath(context) + pngFile;
        } else if (fileExists(jpgFile, context)) {
            fileName = getLyricsDirectoryPath(context) + fileNameWithoutExtension + ".jpg";
        }
        return fileName;
    }

    public static String getFilePathFromSong(Song song, String extension, Context context) {
        return getLyricsDirectoryPath(context) + getFileNameWithoutExtension(replaceChars(song.getTrack()), replaceChars(song.getArtist())) + "." + extension;
    }

    private static String getFileNameFromSong(Song song) {
        return getFileNameWithoutExtension(replaceChars(song.getTrack()), replaceChars(song.getArtist())) + "." + Constants.FILE_TYPE_JPG;
    }

    public static String getFileName(String track, String artist, String extension) {
        return getFileNameWithoutExtension(replaceChars(track), replaceChars(artist)) + "." + extension;
    }

    public static boolean fileExists(String fileName, Context context) {
        File file = new File(getLyricsDirectory(context), fileName);
        return file.exists();
    }

    public static void saveFileToStorage(Bitmap bitmapImage, Song song, Context context) {

        File file = new File(getLyricsDirectory(context), getFileNameFromSong(song));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.e(TAG, "IOException: error saving file to storage " + e.getLocalizedMessage());
        }
    }

    public static void downloadFileFromURL(String fileName, String fileURL, Context context) {
        File file = new File(getLyricsDirectory(context), fileName);
        try(OutputStream output = new FileOutputStream(file)) {
            URL url = new URL(fileURL);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(connection.getInputStream());


            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            input.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException: error downloading file: " + fileName + " from URL:" + fileURL);
        }
    }

    public static String readFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static File getFileFromSong(String fileType, Song song, Context context) {
        File file = null;
        if (Constants.FILE_TYPE_LRC.equalsIgnoreCase(fileType)) {
            file = new File(getLyricsDirectory(context), getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_LRC));
        }
        if (Constants.FILE_TYPE_TXT.equalsIgnoreCase(fileType)) {
            file = new File(getLyricsDirectory(context), getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_TXT));
        }
        if (Constants.FILE_TYPE_PNG.equalsIgnoreCase(fileType)) {
            file = new File(getLyricsDirectory(context), getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_PNG));
        }
        if (Constants.FILE_TYPE_JPG.equalsIgnoreCase(fileType)) {
            file = new File(getLyricsDirectory(context), getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_JPG));
        }
        return file;
    }

    public static File getFileFromSong(Song song, Context context) {
        File path = getLyricsDirectory(context);
        return new File(path, getFileName(song.getTrack(), song.getArtist(), Constants.FILE_TYPE_TXT));
    }

    public static File getFileFromSongByFileType(Song song, String extension, Context context) {
        File path = getLyricsDirectory(context);
        return new File(path, getFileName(song.getTrack(), song.getArtist(), extension));
    }


    public static void writeToFile(String lyricsText, File lyricsFile) {
        if (!StringUtil.isBlank(lyricsText) && !lyricsFile.exists()) {
            try (FileWriter fileWriter = new FileWriter(lyricsFile)) {
                fileWriter.write(lyricsText);
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createNoMediaFile(Context context) {
        String fileName = ".nomedia";
        File output = new File(getLyricsDirectory(context), fileName);
        try {
            if (!fileExists(fileName, context) && !output.createNewFile()) {
               // Log.e(TAG, "Directory not created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static boolean isP() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);
    }

    public static boolean isO() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    public static boolean isPreM() {
        return false;
    }

    /*public static void registerMusicReceiver(Context context, BroadcastReceiver mReceiver) {
        IntentFilter iF = new IntentFilter();

        iF.addAction("com.jrtstudio.music.playstatechanged");
        iF.addAction("com.jrtstudio.music.metachanged");
        iF.addAction("com.jrtstudio.AnotherMusicPlayer.metachanged");
        iF.addAction("com.jrtstudio.AnotherMusicPlayer.playstatechanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("com.htc.music.playstatechanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.rdio.android.playstatechanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.miui.player.playstatechanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.samsung.MusicPlayer.metachanged");
        iF.addAction("com.samsung.sec.metachanged");
        iF.addAction("com.samsung.music.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.playstatechanged");
        iF.addAction("com.lge.music.metachanged");
        iF.addAction("com.lge.music.playstatechanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.sec.android.app.music.playstatechanged");
        iF.addAction("com.rhapsody.metachanged");
        iF.addAction("com.rhapsody.playstatechanged");
        iF.addAction("com.maxmpz.audioplayer.playstatechanged");
        iF.addAction("net.jjc1138.android.scrobbler.action.MUSIC_STATUS");
        iF.addAction("com.adam.aslfms.notify.playstatechanged");
        iF.addAction("com.andrew.apollo.metachanged");
        iF.addAction("com.amazon.mp3.playstatechanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.spotify.music.playbackstatechanged");
        iF.addAction("com.spotify.music.metadatachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.nullsoft.winamp.playstatechanged");
        iF.addAction("com.jetappfactory.jetaudio.playstatechanged");
        iF.addAction("com.jetappfactory.jetaudio.metachanged");
        iF.addAction("com.jetappfactory.jetaudioplus.playstatechanged");
        iF.addAction("com.jetappfactory.jetaudioplus.metachanged");
        iF.addAction("com.e8tracks.playstatechanged");
        iF.addAction("com.e8tracks.metachanged");
        iF.addAction("com.doubleTwist.androidPlayer.metachanged");
        iF.addAction("com.doubleTwist.androidPlayer.playstatechanged");
        iF.addAction("com.tbig.playerpro.playstatechanged");
        iF.addAction("com.tbig.playerpro.metachanged");
        iF.addAction("com.tbig.playerprotrial.playstatechanged");
        iF.addAction("com.tbig.playerprotrial.metachanged");

        context.registerReceiver(mReceiver, iF);
    }*/


    public static int getCurrentTheme(String currentTheme) {
        int selectedTheme;
        switch (currentTheme) {
            case "5":
                selectedTheme = R.style.AppTheme_PitchBlack;
                break;
            case "4":
                selectedTheme = R.style.AppTheme_SuperDark;
                break;
            case "3":
                selectedTheme = R.style.AppTheme_Black;
                break;
            case "2":
                selectedTheme = R.style.AppTheme_Dark;
                break;
            case "1":
            default:
                selectedTheme = R.style.AppTheme;
                break;
        }
        return selectedTheme;
    }

    public static void disableButton(Button button) {
        button.setVisibility(View.INVISIBLE);
    }


    public static void deleteLyricsFile(Song song, Context context) {
        String[] fileTypesToDelete = {Constants.FILE_TYPE_TXT, Constants.FILE_TYPE_LRC, Constants.FILE_TYPE_PNG, Constants.FILE_TYPE_JPG};

        for (String fileType : fileTypesToDelete) {
            File file = getFileFromSong(fileType, song, context);
            if (file != null && file.exists()) {
               file.delete();
            }
        }
    }

    public static String getShortName(String value) {
        if (value.contains(",")) {
            value = value.split(",")[0];
        }
        if (value.contains("(")) {
            value = value.split("\\(")[0];
        }
        if (value.contains("[")) {
            value = value.split("\\[")[0];
        }

        if (value.contains("ft.")) {
            value = value.split("ft")[0];
        }
        if (value.contains("feat.")) {
            value = value.split("feat.")[0];
        }
        return value.trim();
    }


    public static List<String> getSelectedAppsList(String excludedApps) {
        List<String> selectedAppsList = new ArrayList<>();
        //String excludedApps = Utils.getPreferenceStringByKey(context, Constants.PREF_EXCLUDE_APPS, "");
        excludedApps = excludedApps.replace("[", "").replace("]", "").trim();

        String[] selectedApps;
        if (!StringUtil.isBlank(excludedApps)) {
            selectedApps = excludedApps.split(", ");
            Collections.addAll(selectedAppsList, selectedApps);
        }
        return selectedAppsList;
    }

    public static Map<String, String> getInstalledAppList(Context context) {

        final PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //intent.addCategory(Intent.CATEGORY_APP_MUSIC);
        List<ResolveInfo> resInfoList = packageManager.queryIntentActivities(intent, 0);

        Map<String, String> appInfoMap = new TreeMap<>();

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            appInfoMap.put(getAppNameFromPackage(packageManager, packageName), packageName);
        }
        return appInfoMap;
    }

    public static boolean[] getValueIndexes(CharSequence[] installedApps, List<String> selectedApps) {
        boolean[] checkedApps = new boolean[installedApps.length];
        for (int i = 0; i < installedApps.length; i++) {
            checkedApps[i] = selectedApps.contains(installedApps[i].toString());
        }
        return checkedApps;
    }

    public static String getAppNameFromPackage(PackageManager packageManager, String packageName) {
        String appName = null;
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            appName = packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            //Do Nothing
        }
        return appName;
    }

}
