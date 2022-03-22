package com.mapbox.services.android.navigation.v5.navigation.telemetry;


class EnvironmentChain {

  EnvironmentResolver setup() {
    EnvironmentResolver com = new ComServerInformation();
    EnvironmentResolver staging = new StagingServerInformation();
    staging.nextChain(com);
    EnvironmentResolver rootOfTheChain = new ChinaServerInformation();
    rootOfTheChain.nextChain(staging);

    return rootOfTheChain;
  }
}
