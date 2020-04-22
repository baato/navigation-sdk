package com.bhawak.osmnavigation;

import android.app.Application;

import com.mapbox.mapboxsdk.BuildConfig;

import timber.log.Timber;

public class OSMNavigation extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
}
