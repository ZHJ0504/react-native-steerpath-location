package com.reactnativesteerpathlocation.sqllite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.reactnativesteerpathlocation.Setup;

import java.util.ArrayList;

public class DBLoader {

  private final Context context;

  public DBLoader(Context context) {
    this.context = context;
  }

  public void addSetup(Setup setup) {
    SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
    ContentValues values = setValues(setup);

    db.insert(DBContract.SetupEntry.TABLE_NAME, null, values);
    db.close();
  }

  private ContentValues setValues(Setup setup) {
    ContentValues values = new ContentValues();

    values.put(DBContract.SetupEntry.COLUMN_NAME_apikey, setup.getApikey());
    values.put(DBContract.SetupEntry.COLUMN_NAME_name, setup.getName());
    values.put(DBContract.SetupEntry.COLUMN_NAME_region, setup.getRegion());
    values.put(DBContract.SetupEntry.COLUMN_NAME_user_number, setup.getUser_num());

    if (setup.getLiveApikey() != null) {
      values.put(DBContract.SetupEntry.COLUMN_NAME_live_apikey, setup.getLiveApikey());
    }

    return values;
  }

  // TODO: update setup?

  public void deleteAll() {
    SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
    db.execSQL("delete from " + DBContract.SetupEntry.TABLE_NAME);
  }

  public ArrayList<Setup> loadSetups() {
    SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
    Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.SetupEntry.TABLE_NAME, null);
    ArrayList<Setup> setups = new ArrayList<>(cursor.getCount());

    while (cursor.moveToNext()) {

      Setup setup = new Setup()
        .accessToken(cursor.getString(1))
        .name(cursor.getString(2))
        .region(cursor.getString(3))
        .userNumber(cursor.getString(4))
        .liveAccessToken(cursor.getString(5));
      setups.add(setup);
    }

    cursor.close();
    db.close();

    return setups;
  }
}
