package com.bhawak.osmnavigation;

import android.app.Application;
import android.content.Context;

import com.mapbox.mapboxsdk.BuildConfig;

import timber.log.Timber;

public class OSMNavigation extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
    public static Context getContext(){
        return mContext;
    }
}
