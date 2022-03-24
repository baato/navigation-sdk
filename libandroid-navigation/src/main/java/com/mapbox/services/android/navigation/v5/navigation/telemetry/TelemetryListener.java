package com.mapbox.services.android.navigation.v5.navigation.telemetry;

public interface TelemetryListener {

  void onHttpResponse(boolean successful, int code);

  void onHttpFailure(String message);
}
