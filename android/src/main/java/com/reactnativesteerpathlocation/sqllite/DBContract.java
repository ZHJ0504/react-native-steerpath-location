package com.reactnativesteerpathlocation.sqllite;

import android.provider.BaseColumns;

class DBContract {

  private DBContract() {
  }

  static class SetupEntry implements BaseColumns {
    static final String TABLE_NAME = "apikeys";
    static final String COLUMN_NAME_apikey = "apikey";
    static final String COLUMN_NAME_name = "name";
    static final String COLUMN_NAME_region = "region";
    static final String COLUMN_NAME_user_number = "user_number";

    static final String COLUMN_NAME_live_apikey = "liveApikey";
  }
}
