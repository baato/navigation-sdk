package com.mapbox.services.android.navigation.v5.navigation.telemetry;

import android.content.Context;

interface AudioTypeResolver {
  void nextChain(AudioTypeResolver chain);

  String obtainAudioType(Context context);
}