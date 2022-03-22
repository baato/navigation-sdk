package com.mapbox.services.android.navigation.v5.navigation.telemetry;

interface SchedulerFlusher {

  void register();

  void schedule(long elapsedRealTime);

  void unregister();
}
