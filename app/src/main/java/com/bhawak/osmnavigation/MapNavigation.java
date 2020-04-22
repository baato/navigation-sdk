package com.bhawak.osmnavigation;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;

public class MapNavigation extends MapboxNavigation {
    public MapNavigation(@NonNull Context context, @NonNull String accessToken) {
        super(context, accessToken);
    }

    public MapNavigation(@NonNull Context context, @NonNull String accessToken, @NonNull MapboxNavigationOptions options) {
        super(context, accessToken, options);
    }

    public MapNavigation(@NonNull Context context, @NonNull String accessToken, @NonNull MapboxNavigationOptions options, @NonNull LocationEngine locationEngine) {
        super(context, accessToken, options, locationEngine);
    }
}
