package com.bhawak.osmnavigation.navigation;

import androidx.annotation.Nullable;

import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.notification.NavigationNotification;

public class Builder extends MapboxNavigationOptions {
    @Override
    public double maxTurnCompletionOffset() {
        return 0;
    }

    @Override
    public double maneuverZoneRadius() {
        return 0;
    }

    @Override
    public double maximumDistanceOffRoute() {
        return 0;
    }

    @Override
    public double deadReckoningTimeInterval() {
        return 0;
    }

    @Override
    public double maxManipulatedCourseAngle() {
        return 0;
    }

    @Override
    public double userLocationSnapDistance() {
        return 0;
    }

    @Override
    public int secondsBeforeReroute() {
        return 0;
    }

    @Override
    public boolean defaultMilestonesEnabled() {
        return true;
    }

    @Override
    public boolean snapToRoute() {
        return false;
    }

    @Override
    public boolean enableOffRouteDetection() {
        return false;
    }

    @Override
    public boolean enableFasterRouteDetection() {
        return false;
    }

    @Override
    public boolean manuallyEndNavigationUponCompletion() {
        return false;
    }

    @Override
    public double metersRemainingTillArrival() {
        return 0;
    }
    @Override
    public boolean isFromNavigationUi() {
        return false;
    }

    @Override
    public double minimumDistanceBeforeRerouting() {
        return 0;
    }

    @Override
    public boolean isDebugLoggingEnabled() {
        return false;
    }

    @Nullable
    @Override
    public NavigationNotification navigationNotification() {
        return null;
    }

    @Override
    public int roundingIncrement() {
        return 0;
    }

    @Override
    public int timeFormatType() {
        return 0;
    }

    @Override
    public int locationAcceptableAccuracyInMetersThreshold() {
        return 0;
    }


    @Override
    public Builder toBuilder() {
        return null;
    }
}
