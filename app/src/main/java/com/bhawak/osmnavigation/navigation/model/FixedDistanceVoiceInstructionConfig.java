package com.bhawak.osmnavigation.navigation.model;

import android.util.Log;

//import com.graphhopper.util.TranslationMap;

import androidx.annotation.Keep;

import java.util.Locale;

public class FixedDistanceVoiceInstructionConfig extends VoiceInstructionConfig {
//    private final int distanceAlongGeometry; // distance in meter in which the instruction should be spoken
//    private final int distanceVoiceValue; // distance in required unit. f.e: 1km, 300m or 2mi
//
//    public FixedDistanceVoiceInstructionConfig(String key, TranslationMap navigateResponseConverterTranslationMap, Locale locale, int distanceAlongGeometry, int distanceVoiceValue) {
//        super(key, navigateResponseConverterTranslationMap, locale);
//        this.distanceAlongGeometry = distanceAlongGeometry;
//        this.distanceVoiceValue = distanceVoiceValue;
//    }
//
//    @Override
//    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
//        if (distance >= distanceAlongGeometry) {
////            String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue) + " " + turnDescription;
//            String continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue);
//            if (key == "in_km_singular" ||
//                    key == "in_km" ||
//                    key == "for_km") {
//                continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue*1000);
//            }
//            String totalDescription = continueText + turnDescription;
//            Log.d("print description", totalDescription);
//            return new VoiceInstructionValue(distanceAlongGeometry, totalDescription);
//        }
//        return null;
//    }

    private final int distanceAlongGeometry; // distance in meter in which the instruction should be spoken
    private final int distanceVoiceValue; // distance in required unit. f.e: 1km, 300m or 2mi

    public FixedDistanceVoiceInstructionConfig(String key, BaatoTranslationMap navigateResponseConverterTranslationMap, Locale locale, int distanceAlongGeometry, int distanceVoiceValue) {
        super(key, navigateResponseConverterTranslationMap, locale);
        this.distanceAlongGeometry = distanceAlongGeometry;
        this.distanceVoiceValue = distanceVoiceValue;
    }

    @Override
    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
        if (distance >= distanceAlongGeometry) {
            String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue) + " " + turnDescription;
            return new VoiceInstructionValue(distanceAlongGeometry, totalDescription);
        }
        return null;
    }

}
