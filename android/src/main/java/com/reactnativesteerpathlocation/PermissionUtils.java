package com.reactnativesteerpathlocation;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionUtils {

    public static String[] getMissingPermissions(Context context) {
        return getMissingPermissions(context, getPermissions());
    }

    private static String[] getPermissions() {
        return new String[]{ Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN };
    }

    private static String[] getMissingPermissions(Context context, String[] permissions) {
        ArrayList<String> tmp = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                tmp.add(permission);
            }
        }

        return tmp.toArray(new String[tmp.size()]);
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        boolean shouldShowRationale = shouldShowRationale(activity, permissions);

        // Should we show an explanation?
        if (shouldShowRationale) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            showPermissionRationaleDialog(activity, permissions, requestCode);
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                    activity,
                    // just throw them all in.  Android will ask only for the ones it needs.
                    permissions,
                    requestCode);
        }
    }

    private static boolean shouldShowRationale(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }

        return false;
    }

    private static void showPermissionRationaleDialog(final Activity activity, final String[] permissions, final int requestCode) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("App needs permissions")
                .setMessage("YAK requests Bluetooth and Location permissions")
                .setCancelable(true)
                .setPositiveButton("Got it", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    // retry?
                    ActivityCompat.requestPermissions(
                            activity,
                            // just throw them all in.  Android will ask only for the ones it needs.
                            permissions,
                            requestCode);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                .create();

        dialog.show();
    }

    private PermissionUtils() {

    }
}
