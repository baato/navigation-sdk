

package com.mapbox.services.android.navigation.v5.navigation;

import android.location.Location;

// Generated by com.google.auto.value.processor.AutoValueProcessor
 final class AutoValue_NavigationLocationUpdate extends NavigationLocationUpdate {

  private final Location location;
  private final MapboxNavigation mapboxNavigation;

  AutoValue_NavigationLocationUpdate(
      Location location,
      MapboxNavigation mapboxNavigation) {
    if (location == null) {
      throw new NullPointerException("Null location");
    }
    this.location = location;
    if (mapboxNavigation == null) {
      throw new NullPointerException("Null mapboxNavigation");
    }
    this.mapboxNavigation = mapboxNavigation;
  }

  @Override
  Location location() {
    return location;
  }

  @Override
  MapboxNavigation mapboxNavigation() {
    return mapboxNavigation;
  }

  @Override
  public String toString() {
    return "NavigationLocationUpdate{"
         + "location=" + location + ", "
         + "mapboxNavigation=" + mapboxNavigation
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NavigationLocationUpdate) {
      NavigationLocationUpdate that = (NavigationLocationUpdate) o;
      return (this.location.equals(that.location()))
           && (this.mapboxNavigation.equals(that.mapboxNavigation()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= location.hashCode();
    h$ *= 1000003;
    h$ ^= mapboxNavigation.hashCode();
    return h$;
  }

}
