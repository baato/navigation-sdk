package com.mapbox.services.android.navigation.v5.navigation.telemetry;


import android.os.Bundle;

interface EnvironmentResolver {
  void nextChain(EnvironmentResolver chain);

  ServerInformation obtainServerInformation(Bundle appMetaData);
}