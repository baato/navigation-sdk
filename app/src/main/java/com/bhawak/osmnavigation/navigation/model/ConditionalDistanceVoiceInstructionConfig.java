package com.bhawak.osmnavigation.navigation.model;

//import com.graphhopper.util.TranslationMap;

import java.util.Locale;

public class ConditionalDistanceVoiceInstructionConfig extends VoiceInstructionConfig {
    private final int[] distanceAlongGeometry; // distances in meter in which the instruction should be spoken
    private final int[] distanceVoiceValue; // distances in required unit. f.e: 1km, 300m or 2mi

//    public ConditionalDistanceVoiceInstructionConfig(String key, TranslationMap navigateResponseConverterTranslationMap, Locale locale, int[] distanceAlongGeometry, int[] distanceVoiceValue) {
//        super(key, navigateResponseConverterTranslationMap, locale);
//        this.distanceAlongGeometry = distanceAlongGeometry;
//        this.distanceVoiceValue = distanceVoiceValue;
//        if (distanceAlongGeometry.length != distanceVoiceValue.length) {
//            throw new IllegalArgumentException("distanceAlongGeometry and distanceVoiceValue are not same size");
//        }
//    }
//
//    private int getFittingInstructionIndex(double distanceMeter) {
//        for (int i = 0; i < distanceAlongGeometry.length; i++) {
//            if (distanceMeter >= distanceAlongGeometry[i]) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    @Override
//    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
//        int instructionIndex = getFittingInstructionIndex(distance);
//        if (instructionIndex < 0) {
//            return null;
//        }
////        String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue[instructionIndex]) + " " + turnDescription + thenVoiceInstruction;
//        String continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue[instructionIndex]);
//        if (key == "in_km_singular" ||
//                key == "in_km" ||
//                key == "for_km") {
//            continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue[instructionIndex]*1000);
//        }
//        String totalDescription = continueText + turnDescription + thenVoiceInstruction;
//        int spokenDistance = distanceAlongGeometry[instructionIndex];
//        return new VoiceInstructionValue(spokenDistance, totalDescription);
//    }

    public ConditionalDistanceVoiceInstructionConfig(String key, BaatoTranslationMap navigateResponseConverterTranslationMap, Locale locale, int[] distanceAlongGeometry, int[] distanceVoiceValue) {
        super(key, navigateResponseConverterTranslationMap, locale);
        this.distanceAlongGeometry = distanceAlongGeometry;
        this.distanceVoiceValue = distanceVoiceValue;
        if (distanceAlongGeometry.length != distanceVoiceValue.length) {
            throw new IllegalArgumentException("distanceAlongGeometry and distanceVoiceValue are not same size");
        }
    }

    private int getFittingInstructionIndex(double distanceMeter) {
        for (int i = 0; i < distanceAlongGeometry.length; i++) {
            if (distanceMeter >= distanceAlongGeometry[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
        int instructionIndex = getFittingInstructionIndex(distance);
        if (instructionIndex < 0) {
            return null;
        }
        String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue[instructionIndex]) + " " + turnDescription + thenVoiceInstruction;
        int spokenDistance = distanceAlongGeometry[instructionIndex];
        return new VoiceInstructionValue(spokenDistance, totalDescription);
    }
}
