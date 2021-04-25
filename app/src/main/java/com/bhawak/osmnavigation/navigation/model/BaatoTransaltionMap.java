package com.bhawak.osmnavigation.navigation.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.Keep;

import com.bhawak.osmnavigation.OSMNavigation;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
@Keep
class BaatoTranslationMap {
    private static final List<String> LOCALES = Arrays.asList("en_US","ne");
    private final Map<String, Translation> translations = new HashMap();

    public BaatoTranslationMap() {
    }

    public static int countOccurence(String phrase, String splitter) {
        return Helper.isEmpty(phrase) ? 0 : phrase.trim().split(splitter).length;
    }

    public BaatoTranslationMap doImport(String folder) {
        AssetManager gi = OSMNavigation.getContext().getAssets();
        try {
            Iterator var2 = LOCALES.iterator();

            while(var2.hasNext()) {
                String locale = (String)var2.next();
                BaatoTranslationMap.TranslationHashMap trMap = new BaatoTranslationMap.TranslationHashMap(Helper.getLocale(locale));

                InputStream is = gi.open(folder+locale + ".txt");
                trMap.doImport(is);
                this.add(trMap);
            }

            this.postImportHook();
            return this;
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    public BaatoTranslationMap doImport() {
        try {
            Iterator var1 = LOCALES.iterator();

            while(var1.hasNext()) {
                String locale = (String)var1.next();
                TranslationHashMap trMap = new TranslationHashMap(Helper.getLocale(locale));
                trMap.doImport(BaatoTranslationMap.class.getResourceAsStream(locale + ".txt"));
                this.add(trMap);
            }

            this.postImportHook();
            return this;
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    public void add(Translation tr) {
        Locale locale = tr.getLocale();
        this.translations.put(locale.toString(), tr);
        if (!locale.getCountry().isEmpty() && !this.translations.containsKey(tr.getLanguage())) {
            this.translations.put(tr.getLanguage(), tr);
        }

        if ("iw".equals(locale.getLanguage())) {
            this.translations.put("he", tr);
        }

        if ("in".equals(locale.getLanguage())) {
            this.translations.put("id", tr);
        }

    }

    public Translation getWithFallBack(Locale locale) {
        Translation tr = this.get(locale.toString());
        if (tr == null) {
            tr = this.get(locale.getLanguage());
            if (tr == null) {
                tr = this.get("en");
            }
        }

        return tr;
    }

    public Translation get(String locale) {
        locale = locale.replace("-", "_");
        Translation tr = (Translation)this.translations.get(locale);
        if (locale.contains("_") && tr == null) {
            tr = (Translation)this.translations.get(locale.substring(0, 2));
        }

        return tr;
    }
    @SuppressWarnings("ReturnValueIgnored")
    private void postImportHook() {
        Map<String, String> enMap = this.get("en").asMap();
        StringBuilder sb = new StringBuilder();
        Iterator var3 = this.translations.values().iterator();

        while(var3.hasNext()) {
            Translation tr = (Translation)var3.next();
            Map<String, String> trMap = tr.asMap();
            Iterator var6 = enMap.entrySet().iterator();

            while(var6.hasNext()) {
                Entry<String, String> enEntry = (Entry)var6.next();
                String value = (String)trMap.get(enEntry.getKey());
                if (Helper.isEmpty(value)) {
                    trMap.put(enEntry.getKey(), enEntry.getValue());
                } else {
                    int expectedCount = countOccurence((String)enEntry.getValue(), "\\%");
                    if (expectedCount != countOccurence(value, "\\%")) {
                        sb.append(tr.getLocale()).append(" - error in ").append((String)enEntry.getKey()).append("->").append(value).append("\n");
                    } else {
                        Object[] strs = new String[expectedCount];
                        Arrays.fill(strs, "tmp");

                        try {
                            String.format(Locale.ROOT, value, strs);
                        } catch (Exception var12) {
                            sb.append(tr.getLocale()).append(" - error ").append(var12.getMessage()).append("in ").append((String)enEntry.getKey()).append("->").append(value).append("\n");
                        }
                    }
                }
            }
        }

        if (sb.length() > 0) {
            System.out.println(sb);
            throw new IllegalStateException(sb.toString());
        }
    }

    public String toString() {
        return this.translations.toString();
    }

    public static class TranslationHashMap implements Translation {
        final Locale locale;
        private final Map<String, String> map = new HashMap();

        public TranslationHashMap(Locale locale) {
            this.locale = locale;
        }

        public void clear() {
            this.map.clear();
        }

        public Locale getLocale() {
            return this.locale;
        }

        public String getLanguage() {
            return this.locale.getLanguage();
        }

        public String tr(String key, Object... params) {
            String val = (String)this.map.get(Helper.toLowerCase(key));
            return Helper.isEmpty(val) ? key : String.format(Locale.ROOT, val, params);
        }

        public TranslationHashMap put(String key, String val) {
            String existing = (String)this.map.put(Helper.toLowerCase(key), val);
            if (existing != null) {
                throw new IllegalStateException("Cannot overwrite key " + key + " with " + val + ", was: " + existing);
            } else {
                return this;
            }
        }

        public String toString() {
            return this.map.toString();
        }

        public Map<String, String> asMap() {
            return this.map;
        }

        public TranslationHashMap doImport(InputStream is) {
            if (is == null) {
                throw new IllegalStateException("No input stream found in class path!?");
            } else {
                try {
                    Iterator var2 = Helper.readFile(new InputStreamReader(is, Helper.UTF_CS)).iterator();

                    while(var2.hasNext()) {
                        String line = (String)var2.next();
                        if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")) {
                            int index = line.indexOf(61);
                            if (index >= 0) {
                                String key = line.substring(0, index);
                                if (key.isEmpty()) {
                                    throw new IllegalStateException("No key provided:" + line);
                                }

                                String value = line.substring(index + 1);
                                if (!value.isEmpty()) {
                                    this.put(key, value);
                                }
                            }
                        }
                    }

                    return this;
                } catch (IOException var7) {
                    throw new RuntimeException(var7);
                }
            }
        }
    }
}