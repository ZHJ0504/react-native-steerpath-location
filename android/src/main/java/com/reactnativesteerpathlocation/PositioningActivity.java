package com.reactnativesteerpathlocation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.reactnativesteerpathlocation.services.Constants;
import com.reactnativesteerpathlocation.services.LocationForegroundService;
import com.steerpath.sdk.location.BluetoothServices;
import com.steerpath.sdk.location.Location;
import com.steerpath.sdk.location.LocationListener;
import com.steerpath.sdk.location.LocationRequest;
import com.steerpath.sdk.location.LocationServices;

/**
 * Get position without a map. Requirements for indoor positioning are:
 * - device supports BLE
 * - proper permissions are granted
 * - Bluetooth is ON
 * - Location Service is ON.
 * <p>
 * Requirement for Location Services may seem odd, but since Android 6.0 Bluetooth scanning does not work without it.
 * <p>
 * For more rant about the issue, visit:
 * https://stackoverflow.com/questions/33045581/location-needs-to-be-enabled-for-bluetooth-low-energy-scanning-on-android-6-0
 * https://issuetracker.google.com/issues/37065090
 * https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
 * <p>
 * NOTE: SteerpathMapFragment's LocateMe-button does requirement checks for you automatically.
 * <p>
 * TIPS & TRICKS #1: blue dot takes some time to appear when user enters MapActivity, can I make it faster?
 * <p>
 * Generally speaking, speed of the positioning startup depends on few factors:
 * - NDD file. When a unresolved beacon is detected, SDK needs to fetch so called NDD file before positioning can start.
 * Basically it contains positioning information and size of the file depends highly on the size of your site.
 * NDD is then cached for faster subsequent startups.
 * - beacon type. EID beacons are slower than UID because SDK needs to fetch some additional EID mapping information in prior to NDD
 * <p>
 * So, without OfflineBundle, positioning depends also on network quality.
 * <p>
 * By default, Example App starts positioning when setMyLocationEnabled() is called, causing a delay in usability flow.
 * To seemingly speed things up, positioning can be started immediately when application starts instead of waiting user to land MapActivity.
 * Consider following flow:
 * <p>
 * 1. When your first Activity starts, you can also start positioning with FusedLocationProviderApi. You need to make sure requirements are met,
 * permissions etc. Now SDK starts looking for nearby beacons even if MapActivity is not opened.
 * 2. User is distracted with SplashScreen/WelcomeScreen/MainScreen or whatnot. Meanwhile SDK is working with the positioning.
 * 3. User lands to MapActivity. In happy case, positioning is ready at this point.
 */

public class PositioningActivity extends AppCompatActivity implements LocationListener {

  private static final int REQUEST_PERMISSIONS = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();

    IntentFilter filter = new IntentFilter();
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    filter.addAction("android.location.PROVIDERS_CHANGED");
    registerReceiver(receiver, filter);

    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      String[] permissions = PermissionUtils.getMissingPermissions(this);
      if (permissions.length > 0) {
        PermissionUtils.requestPermissions(this, permissions, REQUEST_PERMISSIONS);

      } else {
        startPositioning();
      }

    } else {
      // info.setText(getString(R.string.ble_not_supported));
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.i("NOK", "haha");
    // When bluetooth or location services has just been turned off, there might still be
    // a Location update event coming from the pipeline.
    // These checks has no other purpose but to keep infoText reflecting the state of BL or Location Services.
    if (BluetoothServices.isBluetoothOn() && LocationServices.isLocationOn(this)) {
      String newLine = "\n";
      String buffer = "Provider:" +
        newLine +
        location.getProvider() +
        newLine +
        newLine +
        "Lat/Lon:" +
        newLine +
        location.getLatitude() +
        newLine +
        location.getLongitude() +
        newLine +
        newLine +
        "Building:" +
        newLine +
        location +
        newLine +
        newLine +
        "Floor:" +
        newLine +
        location.getFloorIndex();
      // info.setText("location updated");
    }
  }

  @Override
  public void onStatusChanged(String s, int i, Bundle bundle) {

  }

  @Override
  public void onProviderEnabled(String s) {

  }

  @Override
  public void onProviderDisabled(String s) {

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQUEST_PERMISSIONS: {
        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
          if (result == PackageManager.PERMISSION_DENIED) {
            allPermissionsGranted = false;
          }
        }

        if (allPermissionsGranted) {
          startPositioning();
        }
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_ENABLE_BT:
        if (resultCode == RESULT_OK) {
          startPositioning();
        } else {
          //TODO:  User did not enable Bluetooth or an error occurred
        }
        break;

      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }

  }

  @Override
  public void onStop() {
    super.onStop();
    unregisterReceiver(receiver);

    // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
    // Meaning it will keep bluetooth scanner alive and will drain battery.
    // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
    // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
    LocationServices.getFusedLocationProviderApi().removeLocationUpdates(this);
  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
  };

  private void startPositioning() {
    if (checkBluetooth()) {
      if (checkLocationService()) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("location_request", false)) {
          LocationServices.getFusedLocationProviderApi().requestLocationUpdates(createLocationRequestWithGpsEnabled(), this);
        } else {
          LocationServices.getFusedLocationProviderApi().requestLocationUpdates(this);
        }

        Intent startIntent = new Intent(com.reactnativesteerpathlocation.PositioningActivity.this, LocationForegroundService.class);
        startIntent.setAction(Constants.ACTION.START_ACTION);
        startService(startIntent);
      }
    }
  }

  /**
   * By default, SDK has disabled GPS (priority is PRIORITY_STEERPATH_ONLY).
   * With LocationRequest, you may enable GPS and also define paramaters such as how accurate or how frequently positioning is collected.
   * Usually Steerpath advices against of enabling GPS, but this is the way you can do it.
   *
   * @return request
   */
  private static LocationRequest createLocationRequestWithGpsEnabled() {
    LocationRequest request = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    // GPS threshold determines the minimum accuracy that GPS must have in order for automatic bluetooth to GPS switch to happen.
    request.setGpsThreshold(8);
    return request;
  }

  private boolean checkBluetooth() {
    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    BluetoothAdapter adapter = bluetoothManager.getAdapter();

    // Ensures Bluetooth is available on the device and it is enabled. If not,
    // displays a dialog requesting user permission to enable Bluetooth.
    if (adapter == null || !adapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      return false;
    }

    return true;
  }

  private boolean checkLocationService() {
    if (!LocationServices.isLocationOn(this)) {
      // you may want to show some kind of "Enable Location Services? Yes/No" - dialog before going to Settings
      Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      startActivity(intent);
      return false;
    }

    return true;
  }
}
