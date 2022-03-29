package com.reactnativesteerpathlocation;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.steerpath.sdk.common.DeveloperOptions;
import com.steerpath.sdk.common.SteerpathClient;
import com.steerpath.sdk.location.Location;
import com.steerpath.sdk.location.LocationListener;
import com.steerpath.sdk.location.LocationServices;

// Assume that permission is already given as this feature will not check and request for location permission
@ReactModule(name = SteerpathLocationModule.NAME)
public class SteerpathLocationModule extends ReactContextBaseJavaModule implements LocationListener {
  public static final String NAME = "SteerpathLocation";
  private static final String KEY = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQ6IjoxNTUyMzk2NDk1LCJqdGkiOiI2NDU4OGM5Zi1lYTA4LTQ5NGMtYjgwMi04N2ZmNzcyNjg3YzAiLCJzY29wZXMiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYtcHVibGlzaGVkOnIiLCJzdWIiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYifQ.lWGDm-gZda54YItEtzZEuxv8vVy24FfRBzY25sWyqPtA3J3vBFCTz1E-8bam1-WAR2MEkGRRNCfyV1ZQ1NHU3i2mDDLi2NcogGm3ESO1kcXmwj-LiyWUHN3e0IZji0CRCtHHIS6Z4uavQPGpNXzOFq2yX40KoGQuvupHxqFarCb4XGpYiiws3H08cIIaoC70dKNCGthnWajBvXjhENXSG4QzoUutj8TV3OEaohFx2xfS_i7jBYxnauhB86GduVNkCjvXwM5bhknrf6OgFrHMXhaaf2nk82GSElM8y0nGQ2Y7G9KqhCyU2_8ifzeqqno8gvWQiedGLm0uTpL0bZMeNg";
  private static final String REGION = "AP1";
  //Default configuration for client
  public static final String DEFAULT_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZXMiOiJiYXNlOnI7c3RlZXJwYXRoX3N0YXRpYzpyO3N0ZWVycGF0aF9keW5hbWljOnIiLCJtZXRhQWNjZXNzIjoieSIsImp0aSI6IjhlNTA2OWRhLTViNDEtNGYxZS1iYjYzLTE3NmE0Y2FjMDcyOCIsInN1YiI6InN0ZWVycGF0aCIsImVkaXRSaWdodHMiOiIiLCJlaWRBY2Nlc3MiOiJ5In0.in8zIUm_ZlVhmYPhRMsMxShlqCH0nJnof0kRlWyKuQw";
  public static final String DEFAULT_NAME = "SDE4";
  public static final String DEFAULT_REGION = "AP1";
  public static final String DEFAULT_LIVE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZXMiOiJsaXZlOnIsdyIsImp0aSI6ImE0OGE1MTZjLTk5N2EtNDkwNS04ZGZlLTYxMGRjMmIyM2RiMyIsInN1YiI6InN0ZWVycGF0aCJ9.2GN8CMLIcmeK3_TqNmCIt_bx4QPfGn2VXNGv9wV3Fs8";

  private boolean hasStarted = false;
  private final ReactApplicationContext appContext;

  public SteerpathLocationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.appContext = reactContext;
    doConfigureClient(reactContext, false);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @SuppressLint("WrongConstant")
  private static void doConfigureClient(Context context, boolean useMonitor) {
    int developerOptionsWithMonitor;
    if (useMonitor)
      developerOptionsWithMonitor = DeveloperOptions.getDefaultOptions() | DeveloperOptions.WITH_HEALTH_MONITOR;
    else developerOptionsWithMonitor = DeveloperOptions.DISABLED;

    SteerpathClient.StartConfig.Builder builder = new SteerpathClient.StartConfig.Builder()
      .name(NAME)
      .apiKey(KEY)

      // 3. Enables some developer options. PLEASE DISABLE DEVELOPER OPTIONS IN PRODUCTION!
      // This will add "Monitor"-button above "LocateMe"-button as a visual reminder developer options are in use
      // Use logcat filter "Monitor", for example: adb logcat *:S Monitor:V

      .developerOptions(developerOptionsWithMonitor);
    builder.region(REGION);

    SteerpathClient.StartConfig config = builder.build();

    // NOTE: start() will initialize things in background AsyncTask. This is because installing OfflineBundle is potentially time consuming operation
    // and it shouldn't be done in the main thread. For this reason, app should wait onStarted() callback to be invoked before starting using its features.
    SteerpathClient.getInstance().start(context, config, new SteerpathClient.OfflineBundleStartListener() {
      @Override
      public void onMapReady() {
      }

      @Override
      public void onStarted() {
        // Don't let user to access MapActivity before everything is ready.
      }

      @Override
      public void onError(int i, int i1, String s) {

      }
    });

    // If you need to start Telemetry manually, be sure not to call SteerpathClient.StartConfig.Builder.telemetry()
    //delayTelemetryStart();
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
  }

  private void startPositioning() {
    if (checkBluetooth()) {
      if (checkLocationService()) {
        LocationServices.getFusedLocationProviderApi().requestLocationUpdates(this);
        hasStarted = true;
      }
    }
  }

  private Location getLocation() {
    return LocationServices.getFusedLocationProviderApi().getUserLocation();
  }

  private boolean checkBluetooth() {
    final BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(BLUETOOTH_SERVICE);
    BluetoothAdapter adapter = bluetoothManager.getAdapter();

    // Ensures Bluetooth is available on the device and it is enabled. If not,
    // displays a dialog requesting user permission to enable Bluetooth.
    return (adapter != null || adapter.isEnabled());
  }

  private boolean checkLocationService() {
    return LocationServices.isLocationOn(appContext);
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  // Bluetooth and Location Services must be on
  @ReactMethod
  public void start() {
    startPositioning();
  }

  @ReactMethod
  public void hasStarted() {
    WritableMap map = new WritableNativeMap();
    map.putBoolean("started", hasStarted);
    sendEvent(appContext, "getHasStarted", map);
  }

  @ReactMethod
  public void getUserLocation() {
    WritableMap map = new WritableNativeMap();
    if (hasStarted) {
      Location userLocation = getLocation();
      map.putDouble("latitude", userLocation.getLatitude());
      map.putDouble("longitude", userLocation.getLongitude());
      map.putString("buildingRef", userLocation.getBuildingId());
      map.putInt("floorIndex", userLocation.getFloorIndex());
    }
    // otherwise return an empty map
    sendEvent(appContext, "location", map);
  }
}
