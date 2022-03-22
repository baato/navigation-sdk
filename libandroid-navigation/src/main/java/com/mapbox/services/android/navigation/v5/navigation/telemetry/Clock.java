package com.mapbox.services.android.navigation.v5.navigation.telemetry;
import android.os.SystemClock;

class Clock {

  long giveMeTheElapsedRealtime() {
    return SystemClock.elapsedRealtime();
  }
}
