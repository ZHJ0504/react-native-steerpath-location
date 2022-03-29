package com.reactnativesteerpathlocation.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "steerpathdemo.DB";

  private static final int DATABASE_VERSION = 1;

  private static DBHelper INSTANCE;

  public static synchronized DBHelper getInstance(Context context) {
    if (INSTANCE == null) INSTANCE = new DBHelper(context.getApplicationContext());
    return INSTANCE;
  }

  private DBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    final String SQL_CREATE_APIKEY_LIST = "CREATE TABLE " +
      DBContract.SetupEntry.TABLE_NAME + " ( " +
      DBContract.SetupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
      DBContract.SetupEntry.COLUMN_NAME_apikey + " VARCHAR(255) NOT NULL, " +
      DBContract.SetupEntry.COLUMN_NAME_name + " VARCHAR(255) NOT NULL, " +
      DBContract.SetupEntry.COLUMN_NAME_region + " VARCHAR(255) NOT NULL, " +
      DBContract.SetupEntry.COLUMN_NAME_user_number + " VARCHAR(255) NOT NULL ," +
      DBContract.SetupEntry.COLUMN_NAME_live_apikey + " VARCHAR(255))";


    db.execSQL(SQL_CREATE_APIKEY_LIST);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    db.execSQL("DROP TABLE IF EXISTS " + DBContract.SetupEntry.TABLE_NAME);
    onCreate(db);
  }
}
