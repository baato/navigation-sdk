package com.mapbox.services.android.navigation.v5.navigation.telemetry;


import java.util.List;

interface FullQueueCallback {

  void onFullQueue(List<Event> fullQueue);
}
