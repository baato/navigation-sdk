package com.bhawak.osmnavigation.navigation.model;

import android.util.Log;

import androidx.annotation.Keep;

import com.graphhopper.util.TranslationMap;

import java.io.File;
import java.util.Arrays;

import static com.graphhopper.util.Helper.getLocale;

/**
 * Simple class that extends the TranslationMap to add new translations.
 *
 * @author Bhawak
 */
@Keep
public class NavigateResponseConverterTranslationMap extends BaatoTranslationMap {
private String translation;

    public NavigateResponseConverterTranslationMap(String translation) {
        super();
        this.translation = translation;
    }
    public NavigateResponseConverterTranslationMap() {
        super();
        this.translation = "en_US";
    }
    /**
     * This loads the translation files from the specified folder.
     */
    @Override
    public BaatoTranslationMap doImport() {

        try {
            if(translation.equals("ne"))
                for (String locale : Arrays.asList("en_US", translation)) {
                    TranslationHashMap trMap = new TranslationHashMap(getLocale(locale));
                    trMap.put("in_km_singular", "1 kilometer मा");
                    trMap.put("in_km","%1$s kilometer मा");
                    trMap.put("in_m","%1$s meter मा");
                    trMap.put("for_km","अबको %1$s kilometer सम्म");
                    trMap.put("then","त्यसपछि");
    //                trMap.doImport(NavigateResponseConverterTranslationMap.class.getResourceAsStream(locale + ".txt"));
                    add(trMap);
                }
            else
                for (String locale : Arrays.asList("en_US", "en_US")) {
                    TranslationHashMap trMap = new TranslationHashMap(getLocale(locale));
                    trMap.put("in_km_singular", "In 1 kilometer");
                    trMap.put("in_km","In %1$s kilometers");
                    trMap.put("in_m","In %1$s meters");
                    trMap.put("for_km","for %1$s kilometers");
                    trMap.put("then","then");

//                trMap.doImport(NavigateResponseConverterTranslationMap.class.getResourceAsStream(locale + ".txt"));
                    add(trMap);
                }


            //Not accessible, how bad is this?
            //postImportHook();
            return this;
        } catch (Exception ex) {
//            Log.e("error", ex.getMessage());
            throw new RuntimeException(ex);
        }
//        return  null;
    }

}
