package com.reactnativesteerpathlocation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.reactnativesteerpathlocation.services.Constants;
import com.reactnativesteerpathlocation.services.LocationForegroundService;
import com.reactnativesteerpathlocation.sqllite.DBLoader;
import com.steerpath.sdk.assettracking.AssetGateway;
import com.steerpath.sdk.location.BluetoothServices;
import com.steerpath.sdk.location.Location;
import com.steerpath.sdk.location.LocationListener;
import com.steerpath.sdk.location.LocationRequest;
import com.steerpath.sdk.location.LocationServices;
import com.steerpath.sdk.meta.MetaFeature;
import com.steerpath.sdk.meta.MetaLoader;
import com.steerpath.sdk.meta.MetaQuery;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private Intent startIntent2;

    private ArrayList<Setup> apikeys;
    private DBLoader dbLoader;
    private static Setup currentKey;
    private SharedPreferences sp;
    private static ArrayList<MetaFeature> newBuildings;

    private String buffer;
    private String key;
    private String region;

    private int counter = 0;

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private boolean location_permission_enabled = false;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            key = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQ6IjoxNTUyMzk2NDk1LCJqdGkiOiI2NDU4OGM5Zi1lYTA4LTQ5NGMtYjgwMi04N2ZmNzcyNjg3YzAiLCJzY29wZXMiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYtcHVibGlzaGVkOnIiLCJzdWIiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYifQ.lWGDm-gZda54YItEtzZEuxv8vVy24FfRBzY25sWyqPtA3J3vBFCTz1E-8bam1-WAR2MEkGRRNCfyV1ZQ1NHU3i2mDDLi2NcogGm3ESO1kcXmwj-LiyWUHN3e0IZji0CRCtHHIS6Z4uavQPGpNXzOFq2yX40KoGQuvupHxqFarCb4XGpYiiws3H08cIIaoC70dKNCGthnWajBvXjhENXSG4QzoUutj8TV3OEaohFx2xfS_i7jBYxnauhB86GduVNkCjvXwM5bhknrf6OgFrHMXhaaf2nk82GSElM8y0nGQ2Y7G9KqhCyU2_8ifzeqqno8gvWQiedGLm0uTpL0bZMeNg";
            region = "AP1";

            try {
                dbLoader = new DBLoader(this);
                apikeys = dbLoader.loadSetups();
                currentKey = apikeys.get(0);

                Toast.makeText(this, " -- 1", Toast.LENGTH_LONG).show();

                sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DemoApplication.configureClient(getApplicationContext(), currentKey, sp.getBoolean("monitor", false));
                LocalBroadcastManager.getInstance(this).registerReceiver(sdkReadyBroadcastReceiver,
                        new IntentFilter(DemoApplication.BROADCAST_SDK_READY));

            } catch (Exception ex) {
                try {
                    String name_ = "test";

                    dbLoader = new DBLoader(this);
                    dbLoader.deleteAll();
                    Setup newSetup = new Setup()
                            .accessToken(key)
                            .name(name_)
                            .region(region)
                            .userNumber("99");
                    dbLoader.addSetup(newSetup);
                    apikeys = dbLoader.loadSetups();
                    currentKey = apikeys.get(0);

                    Toast.makeText(this, " -- 2", Toast.LENGTH_LONG).show();

                    sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    DemoApplication.configureClient(getApplicationContext(), currentKey, sp.getBoolean("monitor", false));
                    LocalBroadcastManager.getInstance(this).registerReceiver(sdkReadyBroadcastReceiver,
                            new IntentFilter(DemoApplication.BROADCAST_SDK_READY));

                }
                catch (Exception exxx)
                {
                    Log.e("Gella", exxx.toString(),exxx.fillInStackTrace());
                }
        }
    }

    private final BroadcastReceiver sdkReadyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.loadBuildings();
            if (DemoApplication.USES_DEFAULT_CONFIG) DemoApplication.USES_DEFAULT_CONFIG = false;
        }
    };

    public void loadBuildings() {
        MetaQuery.Builder query = new MetaQuery.Builder(this, MetaQuery.DataType.BUILDINGS);
        MetaLoader.load(query.build(), result -> {
            if (newBuildings != null) {
                newBuildings.clear();
            }
            newBuildings = result.getMetaFeatures();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(receiver, filter);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            String[] permissions = PermissionUtils.getMissingPermissions(this);
            if (permissions.length > 0) {
                PermissionUtils.requestPermissions(this, permissions, REQUEST_PERMISSIONS);

                location_permission_enabled = false;

            } else {
                location_permission_enabled = true;
            }

        } else {
            location_permission_enabled = false;
        }
        if (checkLocationPermission()) {
          checkBluetooth();
          checkLocationService();

          if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            String[] permissions = PermissionUtils.getMissingPermissions(getApplicationContext());
            if (permissions.length > 0) {
              PermissionUtils.requestPermissions(getParent(), permissions, REQUEST_PERMISSIONS);
            } else {
              // I think this is the default (should run location)
              try {
                Thread.sleep(000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              DemoApplication.USES_DEFAULT_CONFIG = true;
              startPositioning();

              startIntent2 = new Intent(MainActivity.this, LocationForegroundService.class);
              startIntent2.setAction(Constants.ACTION.START_ACTION);
              startService(startIntent2);
            }
          }
        }
    }

    public void stop() {
        stopService(startIntent2);
        // unregisterReceiver(receiver);

        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
        // Meaning it will keep bluetooth scanner alive and will drain battery.
        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
        LocationServices.getFusedLocationProviderApi().removeLocationUpdates(MainActivity.this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // When bluetooth or location services has just been turned off, there might still be
        // a Location update event coming from the pipeline.
        // These checks has no other purpose but to keep infoText reflecting the state of BL or Location Services.
        Log.i ("NOK", String.valueOf(location.getLongitude()));
        Log.i ("NOK", String.valueOf(location.getLatitude()));
        Log.i ("NOK", String.valueOf(location.getFloorIndex()));
        Log.i ("NOK", String.valueOf(location.getBuildingId()));

        if (BluetoothServices.isBluetoothOn() && LocationServices.isLocationOn(this)) {

            counter += 1;
            long now = System.currentTimeMillis();
            String somTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000").format(new java.util.Date(now)).replace(" ", "T") + "Z";

            //////// data to be sent to the ///////////
            if (counter % 10 == 1) {
                buffer = "{\"id\":\"" + now + "\",\"data\":\"" + "" + somTime + "," + location.getProvider() + "," + "full_user_id" + "," + location.getLongitude() + "," + location.getLatitude() + "," + (location.getFloorIndex() + 1) + "," + location.getAccuracy() + "" + "\"" + "}";

                Log.i ("NOK", "buffer");
            }
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
                    break;
                  }
                }

                if (allPermissionsGranted) {
                    //startPositioning();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // startPositioning();
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();

        // unregisterReceiver(receiver);

        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
        // Meaning it will keep bluetooth scanner alive and will drain battery.
        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
        // LocationServices.getFusedLocationProviderApi().removeLocationUpdates(this);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
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
        request.setGpsThreshold(3);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (SettingsHelper.useAssetGateway()) {
            AssetGateway.stopAssetGatewayService(this);

        }

        unregisterReceiver(receiver);

        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
        // Meaning it will keep bluetooth scanner alive and will drain battery.
        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
        LocationServices.getFusedLocationProviderApi().removeLocationUpdates(this);

    }

    public boolean checkLocationPermission() {
        return true;
    }
}
