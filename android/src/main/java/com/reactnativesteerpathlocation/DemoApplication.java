package com.reactnativesteerpathlocation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.steerpath.sdk.common.DeveloperOptions;
import com.steerpath.sdk.common.SteerpathClient;
import com.steerpath.sdk.live.LiveOptions;
import com.steerpath.sdk.location.LocationServices;
import com.steerpath.sdk.telemetry.TelemetryConfig;

/**
 * Steerpath Indoor Positioning SDK is derived from Mapbox SDK.
 * <p>
 * TROUBLESHOOT: https://s3-eu-west-1.amazonaws.com/steerpath/android/documentation/latest/javadoc/reference/packages.html
 */

public class DemoApplication extends Application {

  private static final String TAG = com.reactnativesteerpathlocation.DemoApplication.class.getSimpleName();

  public static final String BROADCAST_SDK_READY = "steerpath.com.steerpath_android_store_app.broadcast.SDK_READY";

  public static final String PREF_NAME = "demo";
  public static final String KEY_API_KEY = "steerpath_api_key";
  public static final String NO_APIKEY = "no_apikey";

  //Default configuration for client
  public static final String DEFAULT_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZXMiOiJiYXNlOnI7c3RlZXJwYXRoX3N0YXRpYzpyO3N0ZWVycGF0aF9keW5hbWljOnIiLCJtZXRhQWNjZXNzIjoieSIsImp0aSI6IjhlNTA2OWRhLTViNDEtNGYxZS1iYjYzLTE3NmE0Y2FjMDcyOCIsInN1YiI6InN0ZWVycGF0aCIsImVkaXRSaWdodHMiOiIiLCJlaWRBY2Nlc3MiOiJ5In0.in8zIUm_ZlVhmYPhRMsMxShlqCH0nJnof0kRlWyKuQw";
  public static final String DEFAULT_NAME = "SDE4";
  public static final String DEFAULT_REGION = "AP1";
  public static final String DEFAULT_LIVE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZXMiOiJsaXZlOnIsdyIsImp0aSI6ImE0OGE1MTZjLTk5N2EtNDkwNS04ZGZlLTYxMGRjMmIyM2RiMyIsInN1YiI6InN0ZWVycGF0aCJ9.2GN8CMLIcmeK3_TqNmCIt_bx4QPfGn2VXNGv9wV3Fs8";

  public static boolean USES_DEFAULT_CONFIG = false;

  public static Setup currentSetup;

  public static boolean liveEnabled = false;
  private static LiveOptions liveOptions;
  public static boolean showThisDeviceLive = false;

  @Override
  public void onCreate() {
    super.onCreate();

    // NOTE: check LeakCanary.isInAnalyzerProcess() before everything else!
    // It will conveniently filter subsequent calls to onCreate().

    // Memory Leaks
    // if (LeakCanary.isInAnalyzerProcess(this)) {
    // This process is dedicated to LeakCanary for heap analysis.
    // You should not init your app in this process.
    // return;
    //}

    // when setANRListener() is not set, app will crash on ANR
    // TODO: for some reason, ANR is not delivered to Crashlytics id ANRListener is set?
    //new ANRWatchDog().setReportMainThreadOnly().start();

    // Other badness
    //enableStrictMode();

    LocalBroadcastManager.getInstance(this).registerReceiver(
      apikeyReadyBroadcastReceiver, new IntentFilter(SetupHelper.BROADCAST_APIKEY_READY));
  }

  public static Setup getDefaultSetup() {

    return new Setup()
      .accessToken(DEFAULT_APIKEY)
      .name(DEFAULT_NAME)
      .region(DEFAULT_REGION)
      .liveAccessToken(DEFAULT_LIVE_APIKEY);
  }

  /*
   * Broadcast for receiving api key, if no api key found we use default key
   */
  private BroadcastReceiver apikeyReadyBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Setup setup = intent.getParcelableExtra("setup");
      boolean useMonitor = intent.getBooleanExtra("monitor", false);
      configureClient(getApplicationContext(), setup, useMonitor);
    }
  };

  public static void configureClient(Context context, Setup setup, boolean useMonitor) {
    doConfigureClient(context, setup, useMonitor);
  }

  @SuppressLint("WrongConstant")
  private static void doConfigureClient(Context context, Setup setup, boolean useMonitor) {

    SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);

    currentSetup = setup;

    int developerOptionsWithMonitor;
    if (useMonitor)
      developerOptionsWithMonitor = DeveloperOptions.getDefaultOptions() | DeveloperOptions.WITH_HEALTH_MONITOR;
    else developerOptionsWithMonitor = DeveloperOptions.DISABLED;

    /*
     * OPTIONAL: Steerpath Telemetry collects user location and beacon data and sends them to backend for further processing.
     * This tool is for very large venues and it is recommended to consider more light-weight solutions before enabling
     * Steerpath Telemetry.
     *
     * For required tokens, contact support@steerpath.com
     */
    TelemetryConfig telemetry = new TelemetryConfig.Builder(context)
      .accessToken("YOUR_TELEMETRY_ACCESS_TOKEN")
      .baseUrl("YOUR_TELEMETRY_URL")
      .build();

    SteerpathClient.StartConfig.Builder builder = new SteerpathClient.StartConfig.Builder()
      .name(setup.getName())
      .apiKey(setup.getApikey())

      // OPTIONAL:
      // 1. OfflineBundle contains metadata, style, positioning, routing and vector tile data. Makes map features usable with bad
      // network conditions or without network at all.
      // For obtaining OfflineBundle, contact support@steerpath.com
      // .sff file must be located in /assets -folder
      // If your setup contains many large buildings and you have low end device, first initial start() may take awhile.
      // Subsequent calls are much faster.
      //.installOfflineBundle("sp_offline_data_20170703T055713Z.sff")

      // 2. Enables Steerpath Telemetry
      .telemetry(telemetry)

      // 3. Enables some developer options. PLEASE DISABLE DEVELOPER OPTIONS IN PRODUCTION!
      // This will add "Monitor"-button above "LocateMe"-button as a visual reminder developer options are in use
      // Use logcat filter "Monitor", for example: adb logcat *:S Monitor:V

      .developerOptions(developerOptionsWithMonitor);

    if (!setup.getRegion().equals(DEFAULT_REGION)) {
      builder.region(setup.getRegion());
    }

    if (defaultPrefs.getBoolean("diagnostics", true)) {
      builder.telemetry(getTelemetryConfig(context));
    }

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
        notifyReady(context);
        setGuideOptions(context);
      }

      @Override
      public void onError(int i, int i1, String s) {

      }
    });

    // If you need to start Telemetry manually, be sure not to call SteerpathClient.StartConfig.Builder.telemetry()
    //delayTelemetryStart();
  }

  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
      .detectAll()
      .penaltyFlashScreen()
      .penaltyLog()
      .build());

    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
      .detectAll()
      .penaltyLog()
      .build());
  }

  private static void notifyReady(Context context) {
    Intent intent = new Intent(BROADCAST_SDK_READY);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }

  /**
   * If Telemetry needs to be started after {@link SteerpathClient#start(Context, SteerpathClient.StartConfig, SteerpathClient.StartListener)}.
   * Example Scenario: if user id or other property is downloaded from the backend and start of Telemetry must be delayed until response has been received.
   */

//    public static void delayTelemetryStart(Context context) {
//        new Handler().postDelayed(() ->
//                TelemetryService.getInstance().start(context.getApplicationContext(), getTelemetryConfig(context)), 5000);
//    }
  private static TelemetryConfig getTelemetryConfig(Context context) {
    return new TelemetryConfig.Builder(context.getApplicationContext())
      .beaconLevel(TelemetryConfig.BeaconLevel.ALL)
      .accessToken(SetupHelper.getSetup(context.getApplicationContext()).getApikey())
      .baseUrl("https://capture-v1.eu.steerpath.com/v1")
      .backgroundPolicy(TelemetryConfig.BackgroundPolicy.PROCESS)
      .build();
  }

  public static void setLiveOptions(LiveOptions opts) {
    liveOptions = opts;
  }

  public static LiveOptions getLiveOptions() {
    return liveOptions;
  }

  private static void setGuideOptions(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    int rssi = preferences.getInt("rssi", 0);
    LocationServices.getGuideOptions().rssi(rssi);

    SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    LocationServices.getGuideOptions().accelerometer(defaultPrefs.getBoolean("accelerometer", true));
    LocationServices.getGuideOptions().compass(defaultPrefs.getBoolean("compass", false));
    LocationServices.getGuideOptions().gyroscope(defaultPrefs.getBoolean("gyroscope", false));
  }
}
