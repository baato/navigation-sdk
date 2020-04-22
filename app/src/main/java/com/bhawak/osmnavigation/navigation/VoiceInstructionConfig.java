package com.bhawak.osmnavigation.navigation;

import android.util.Log;

import com.graphhopper.util.Helper;
import com.graphhopper.util.TranslationMap;

import java.util.Locale;

//import static com.graphhopper.navigation.DistanceUtils.meterToKilometer;
//import static com.graphhopper.navigation.DistanceUtils.meterToMiles;

abstract class VoiceInstructionConfig extends DistanceUtils {
    protected final String key; // TranslationMap key
    protected final TranslationMap navigateResponseConverterTranslationMap;
    protected Locale locale = Locale.ENGLISH;

    public VoiceInstructionConfig(String key, TranslationMap navigateResponseConverterTranslationMap, Locale locale) {
        this.key = key;
        this.navigateResponseConverterTranslationMap = navigateResponseConverterTranslationMap;
        this.locale = locale;
    }

    class VoiceInstructionValue {
        final int spokenDistance;
        final String turnDescription;

        public VoiceInstructionValue(int spokenDistance, String turnDescription) {
            this.spokenDistance = spokenDistance;
            this.turnDescription = turnDescription;
        }
    }

    public abstract VoiceInstructionValue getConfigForDistance(
            double distance,
            String turnDescription,
            String thenVoiceInstruction);
}


