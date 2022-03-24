package com.mapbox.services.android.navigation.v5.navigation.telemetry;


import android.util.Log;

@SuppressWarnings("LogNotTimber")
class Logger {

  int debug(String tag, String msg) {
    return Log.d(tag, msg);
  }

  int error(String tag, String msg) {
    return Log.e(tag, msg);
  }
}
