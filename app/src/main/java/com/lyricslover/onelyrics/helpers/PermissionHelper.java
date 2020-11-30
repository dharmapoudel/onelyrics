package com.lyricslover.onelyrics.helpers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.receivers.MediaListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PermissionHelper {

    private PermissionHelper() {
    }

    static final String[] storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    public static boolean allPermissionsAvailable(Context context) {
        return isStoragePermissionAvailable(context) && isOverlayPermissionAvailable(context.getApplicationContext()) && isNotificationPermissionAvailable(context);
    }

    public static boolean isStoragePermissionAvailable(Context context) {
        return checkPermissions(context, storagePermissions);
    }


    @TargetApi(23)
    public static boolean isOverlayPermissionAvailablePostO(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static boolean isOverlayPermissionAvailable(Context context) {
        if (Utils.isPreM()) {
            return true;
        } else {
            return isOverlayPermissionAvailablePostO(context);
        }
    }

    public static boolean isNotificationPermissionAvailable(Context context) {
        String notificationListeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return (notificationListeners != null && notificationListeners.contains(MediaListener.class.getName()));
    }

    private static boolean checkPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity, int permissionId) {
        ActivityCompat.requestPermissions(activity, storagePermissions, permissionId);
    }

    public static void verifyAudioPermissions(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        for (String permission : permissions) {
            int p = ContextCompat.checkSelfPermission(activity, permission);
            if (p != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permissions, Constants.WRITE_EXTERNAL);
                break;
            }
        }
    }
}