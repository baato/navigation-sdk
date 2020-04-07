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

class ConditionalDistanceVoiceInstructionConfig extends VoiceInstructionConfig {
    private final int[] distanceAlongGeometry; // distances in meter in which the instruction should be spoken
    private final int[] distanceVoiceValue; // distances in required unit. f.e: 1km, 300m or 2mi

    public ConditionalDistanceVoiceInstructionConfig(String key, TranslationMap navigateResponseConverterTranslationMap, Locale locale, int[] distanceAlongGeometry, int[] distanceVoiceValue) {
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
//        String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue[instructionIndex]) + " " + turnDescription + thenVoiceInstruction;
        String continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue[instructionIndex]);
        if (key == "in_km_singular" ||
                key == "in_km" ||
                key == "for_km") {
            continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue[instructionIndex]*1000);
        }
        String totalDescription = continueText + turnDescription + thenVoiceInstruction;
        int spokenDistance = distanceAlongGeometry[instructionIndex];
        return new VoiceInstructionValue(spokenDistance, totalDescription);
    }
}

class FixedDistanceVoiceInstructionConfig extends VoiceInstructionConfig {
    private final int distanceAlongGeometry; // distance in meter in which the instruction should be spoken
    private final int distanceVoiceValue; // distance in required unit. f.e: 1km, 300m or 2mi

    public FixedDistanceVoiceInstructionConfig(String key, TranslationMap navigateResponseConverterTranslationMap, Locale locale, int distanceAlongGeometry, int distanceVoiceValue) {
        super(key, navigateResponseConverterTranslationMap, locale);
        this.distanceAlongGeometry = distanceAlongGeometry;
        this.distanceVoiceValue = distanceVoiceValue;
    }

    @Override
    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
        if (distance >= distanceAlongGeometry) {
//            String totalDescription = navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue) + " " + turnDescription;
            String continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue);
            if (key == "in_km_singular" ||
                    key == "in_km" ||
                    key == "for_km") {
                continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue*1000);
            }
            String totalDescription = continueText + turnDescription;
            Log.d("print description", totalDescription);
            return new VoiceInstructionValue(distanceAlongGeometry, totalDescription);
        }
        return null;
    }

}


class InitialVoiceInstructionConfig extends VoiceInstructionConfig {
    // The instruction should not be spoken straight away, but wait until the user merged on the new road and can listen to instructions again
    private final int distanceDelay; // delay distance in meter
    private final int distanceForInitialStayInstruction; // min distance in meter for initial instruction
    private final DistanceUtils.Unit unit;
    private final TranslationMap translationMap;

    public InitialVoiceInstructionConfig(String key, TranslationMap translationMap, TranslationMap navigateResponseConverterTranslationMap, Locale locale, int distanceForInitialStayInstruction, int distanceDelay, DistanceUtils.Unit unit) {
        super(key, navigateResponseConverterTranslationMap, locale);
        this.distanceForInitialStayInstruction = distanceForInitialStayInstruction;
        this.distanceDelay = distanceDelay;
        this.unit = unit;
        this.translationMap = translationMap;
    }

    private int distanceAlongGeometry(double distanceMeter) {
        // Cast to full units
        int tmpDistance = (int) (distanceMeter - distanceDelay);
        if (unit == DistanceUtils.Unit.METRIC) {
            return (tmpDistance / 1000) * 1000;
        } else {
            tmpDistance = (int) (tmpDistance * meterToMiles);
            return (int) Math.ceil(tmpDistance / meterToMiles);
        }
    }

    private int distanceVoiceValue(double distanceInMeter) {
        if (unit == DistanceUtils.Unit.METRIC) {
            return (int) (distanceAlongGeometry(distanceInMeter) * meterToKilometer);
        } else {
            return (int) (distanceAlongGeometry(distanceInMeter) * meterToMiles);
        }
    }

    @Override
    public VoiceInstructionValue getConfigForDistance(double distance, String turnDescription, String thenVoiceInstruction) {
        if (distance > distanceForInitialStayInstruction) {
            int spokenDistance = distanceAlongGeometry(distance);
            int distanceVoiceValue = distanceVoiceValue(distance);
            try {
//                String continueDescription = translationMap.getWithFallBack(locale).tr("continue") + " " + navigateResponseConverterTranslationMap.getWithFallBack(locale).tr(key, distanceVoiceValue);
                String continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue);
                if (key == "in_km_singular" ||
                        key == "in_km" ||
                        key == "for_km") {
                    continueText = NavigateResponseConverter.getTranslatedDistance(distanceVoiceValue*1000);
                }
                String continueDescription = translationMap.getWithFallBack(locale).tr("continue") + " " + continueText;
                continueDescription = Helper.firstBig(continueDescription);
                return new VoiceInstructionValue(spokenDistance, continueDescription);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
