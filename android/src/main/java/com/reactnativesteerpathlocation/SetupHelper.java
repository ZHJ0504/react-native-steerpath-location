package com.reactnativesteerpathlocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import com.reactnativesteerpathlocation.sqllite.DBLoader;
import com.reactnativesteerpathlocation.Setup;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class SetupHelper {

  public static final String BROADCAST_APIKEY_READY = "steerpath.com.steerpath_android_store_app.broadcast.APIKEY_READY";

  private static final OkHttpClient DEFAULT_OK_HTTP_CLIENT;
  static {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.connectTimeout(10, TimeUnit.SECONDS);
    builder.writeTimeout(10, TimeUnit.SECONDS);
    builder.writeTimeout(10, TimeUnit.SECONDS);
    DEFAULT_OK_HTTP_CLIENT = builder.build();
  }

  /*
   * Checks the source of APIKEY. Whether to use deeplink or shared preferences. If no apikeys, then we use default apikey.
   */
  public static void initApikey(Context context, Intent intent) {
    String[] deeplink = readDeepLink(intent);
    Setup defaultSetup = DemoApplication.getDefaultSetup();

    if (deeplink != null) {
      if (DemoApplication.USES_DEFAULT_CONFIG) DemoApplication.USES_DEFAULT_CONFIG = false;

      String apikey = deeplink[0];
      String name = deeplink[1];
      String region = deeplink[2];
      String user_number = deeplink[3];
      String liveApikey = deeplink[4];


      name = name.replace("_", " ");

      Setup setup = new Setup()
        .accessToken(apikey)
        .name(name)
        .region(region)
        .userNumber(user_number);

      if (liveApikey != null) {
        setup.liveAccessToken(liveApikey);
      }

      ArrayList<Setup> duplicates = dbLoader.loadSetups();
      if (!duplicates.contains(setup)) {
        dbLoader.addSetup(setup);
      }

      createSetup(context, setup);

      //Add default key to database after first api key has been set
      if (!duplicates.contains(defaultSetup)) {
        dbLoader.addSetup(defaultSetup);
      }

    } else {
      SharedPreferences preferences = context.getSharedPreferences(DemoApplication.PREF_NAME, MODE_PRIVATE);
      String existingApikey = preferences.getString(DemoApplication.KEY_API_KEY, DemoApplication.NO_APIKEY);

      if (existingApikey.equals(DemoApplication.NO_APIKEY)) {
        DemoApplication.USES_DEFAULT_CONFIG = true;
        createSetup(context, defaultSetup);

      } else createSetup(context, getSetup(context));
    }
  }

  public static void writeToSharedPrefs(Context context, Setup setup) {
    SharedPreferences.Editor editor = context.getSharedPreferences(DemoApplication.PREF_NAME, MODE_PRIVATE).edit();

    if (setup == null) {
      editor.remove(DemoApplication.KEY_API_KEY);
    } else {
      StringBuilder strBuilder = new StringBuilder();
      strBuilder.append(setup.getApikey()).append(";").append(setup.getName()).append(";").append(setup.getRegion());
      if (setup.getLiveApikey() != null) {
        strBuilder.append(";").append(setup.getLiveApikey());
      }
      editor.putString(DemoApplication.KEY_API_KEY, strBuilder.toString());
    }

    editor.apply();
  }

  public static Setup getSetup(Context context) {
    SharedPreferences preferences = context.getSharedPreferences(DemoApplication.PREF_NAME, MODE_PRIVATE);
    String currentSetup = preferences.getString(DemoApplication.KEY_API_KEY, DemoApplication.NO_APIKEY);

    if (currentSetup.equals(DemoApplication.NO_APIKEY)) {
      currentSetup = DemoApplication.getDefaultSetup().getApikey() + ";" + DemoApplication.getDefaultSetup().getName() + ";" + DemoApplication.getDefaultSetup().getRegion();
    }

    String[] split = currentSetup.split(";");
    int size = split.length;

    Setup setup = new Setup()
      .accessToken(split[0])
      .name(split[1]);

    if (size >= 3) {
      setup.region(split[2]);
    }

    if (size >= 4) {
      setup.liveAccessToken(split[3]);
    }

    return setup;
  }

  /*
   * Reading deep link intent
   */
  private static String[] readDeepLink(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      String tmp = data.toString();

      String[] deepLink = new String[4];

      // Adding default region - gets overridden if deeplink contains another region
      String region = DemoApplication.DEFAULT_REGION;
      deepLink[2] = region;

      UrlQuerySanitizer query = new UrlQuerySanitizer(tmp);
      for (UrlQuerySanitizer.ParameterValuePair pair : query.getParameterList()) {

        switch (pair.mParameter) {
          case "apikey":
            deepLink[0] = pair.mValue;
            break;
          case "name":
            deepLink[1] = pair.mValue;
            break;
          case "region":
            deepLink[2] = pair.mValue;
            break;
          case "liveApikey":
            deepLink[3] = pair.mValue;
            break;
          case "user_number":
            deepLink[4] = pair.mValue;
            break;
        }
      }

      return deepLink;

    } else {
      return null;
    }
  }

  /*
   * Param apiKey includes both; apikey token and name -> remember to split in Application class
   */
  @SuppressLint("ApplySharedPref")
  private static void createSetup(Context context, Setup setup) {
    if (!DemoApplication.USES_DEFAULT_CONFIG) {
      writeToSharedPrefs(context, setup);
    }
    sendBroadcast(context, setup);
  }

  /*
   * Notify application class that we have found valid apikey
   */
  private static void sendBroadcast(Context context, Setup setup) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    Intent intent = new Intent(BROADCAST_APIKEY_READY);
    intent.putExtra("setup", setup);
    intent.putExtra("monitor", sp.getBoolean("monitor",false));
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }

  // Try to download buildings to verify that key is valid
  public static void verifyApikey(String apikey, Callback callback) {
    HttpUrl url = HttpUrl.get("https://meta2.eu.steerpath.com/meta/v2/buildings");
    Request.Builder builder = new Request.Builder().get();
    builder.url(url);
    builder.header("Authorization", apikey);

    final Request request = builder.build();
    DEFAULT_OK_HTTP_CLIENT.newCall(request).enqueue(callback);
  }
}
