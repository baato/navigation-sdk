package com.mapbox.services.android.navigation.v5.navigation.telemetry;

class AudioTypeChain {

  AudioTypeResolver setup() {
    AudioTypeResolver unknown = new UnknownAudioType();
    AudioTypeResolver speaker = new SpeakerAudioType();
    speaker.nextChain(unknown);
    AudioTypeResolver headphones = new HeadphonesAudioType();
    headphones.nextChain(speaker);
    AudioTypeResolver rootOfTheChain = new BluetoothAudioType();
    rootOfTheChain.nextChain(headphones);

    return rootOfTheChain;
  }
}
