package com.bhawak.osmnavigation.navigation;

import java.util.Locale;

//import static com.graphhopper.navigation.DistanceUtils.meterToKilometer;
//import static com.graphhopper.navigation.DistanceUtils.meterToMiles;

abstract class VoiceInstructionConfig extends DistanceUtils {
    protected final String key; // TranslationMap key
    protected final BaatoTranslationMap navigateResponseConverterTranslationMap;
    protected Locale locale = Locale.ENGLISH;

    public VoiceInstructionConfig(String key, BaatoTranslationMap navigateResponseConverterTranslationMap, Locale locale) {
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


