![GitHub release (latest by date)](https://img.shields.io/github/v/release/baato/navigation-sdk)

# Baato-navigation SDK

The Java SDK makes it easy to consume the Baato-navigation API into existing native android projects. Baato-navigation SDK for android provides you a fine navigation experience.

### Available SDKs

To help easy integration of navigation into your Android application, two SDKs are available, the core **Navigation SDK** and **Navigation UI SDK**

### Getting Started

 1. Open up your project's build.gradle file. Add the following code:

```
allprojects{
 repositories {
  maven { url 'https://jitpack.io' }
 }
}
```

2. Open up your application's build.gradle file. Add the following code:
```
android {
 compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
 }
```

3. Add the dependencies
```
//baato-navigation dependency
dependencies {
  implementation 'com.github.baato.navigation-sdk:baato-navigation-android:${latest-version}'
  implementation 'com.github.baato.navigation-sdk:baato-navigation-android-ui:${latest-version}'
}
```

4. Add other configuration in app level gradle
```
    defaultConfig {
        multiDexEnabled true
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    dependencies {
     // location service by google, use latest stable version
     implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.0.0'
     implementation 'com.google.android.gms:play-services-location:17.0.0'
    }
```

If you are using MapBox as your map service, our library only supports the **version** as mentioned below:
```
   //mapbox sdk
    implementation('com.mapbox.mapboxsdk:mapbox-android-sdk:6.5.0') {
        exclude group: 'group_name', module: 'module_name'
    }
    implementation 'com.mapbox.mapboxsdk:mapbox-android-plugin-locationlayer:0.8.1'
```

### Prerequisites

 Add the following permission to the Manifest file in your Android project
```
// For Notification
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
// For Location
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
// Since app uses baato-services add network services

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Implementation

#### Managing the location permission and location update

1. Implement the PermissionsListener, for navigation to work with better experience you need to provide the runtime location permission
```
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
               //do something so the user see the required permission
    }
    
    @Override
    public void onPermissionResult(boolean granted) {
        if (!granted) {
           //do something
        }
    }
```    
2. Use permission manager to check if location permission is granted
```
       permissionsManager = new PermissionsManager(context);
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            permissionsManager.requestLocationPermissions(context);
        } else {
        // get location and set map component accordingly
            getMyLocation();
        }
```
3. Initilization of Location service to track the user with google api client; implemented methods LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
```
// Note: This is for base map activity. If you are using multiple map instances only add to the base map activity. Once location is calibrated LocationEngineListner will receive updates.

// Initilize once when permissions are granted
 @Override
    public void onPermissionResult(boolean granted) {
        if (granted)
            getMyLocation();
    }
 
 //on google api client connected
 @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }
 
 private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    permissionLocation = checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                
                    //get GPS Location
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    if (mylocation != null) {
                          // update gps
                        locationLayer.forceLocationUpdate(mylocation);
                    }
                    
                    // Initilaizing google location engine
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, (LocationListener) this);
                    PendingResult result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(result1 -> {
                        final Status status = result1.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied.
                                // But could be fixed by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    status.startResolutionForResult(this,
                                            Constants.GPS_REQUEST);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied.
                                // You can initialize location requests here.
                                int permissionLocation1 = 0;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    permissionLocation1 = checkSelfPermission(
                                            Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                                if (permissionLocation1 == PackageManager.PERMISSION_GRANTED) {
                                    mylocation = LocationServices.FusedLocationApi
                                            .getLastLocation(googleApiClient);
                                    originPoint = Point.fromLngLat(mylocation.getLongitude(),
                                            mylocation.getLatitude());
                                   locationLayer.forceLocationUpdate(mylocation);
//                                    locationEngine.activate();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                //finish();
                                break;
                        }
                    });
                }
            } else googleApiClient.connect();
        } else setUpGClient();
    }
    
    //setup google api client
    
    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
 

```

4. Implement the LocationEngineListener for location updates when the map is ready
```
     //Initialize the location engine
     LocationEngineProvider locationEngineProvider = new LocationEngineProvider(context);
    locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
    
    //Set locationEnginePriority
    locationEngine.setPriority(LocationEnginePriority.LOW_POWER);
    
    //Add listner
    locationEngine.addLocationEngineListener(context);
    
    //Location update interval
    locationEngine.setFastestInterval(ONE_SECOND_INTERVAL);
    
    //activate
    locationEngine.activate();
```    
4. Deactivate location engine on destroy method to prevent any memory leak
```   
   if (locationEngine != null) {
      locationEngine.removeLocationEngineListener(context);
    }
```
#### Request a route

You can reuest a route using BaatoNavigationRoute. Follow the implementation details from [Baato-Java Client](https://github.com/baato/java-client). 
Once you receive the route from the baato java-client, you are ready to use Navigation UI SDK.

#### Turn by Turn navigation

You can launch the UI using Navigation launcher from within your activity using current route ( [Request a route](#request-a-route) )

```

//Route fetched from BaatoNavigationRoute
DirectionsRoute currentRoute = ...

boolean simulateRoute=false;

NavigationLauncherOptions options = NavigationLauncherOptions.builder()
        .directionsRoute(currentRoute)
        .shouldSimulateRoute(simulateRoute) // boolean value set true for simulation
        .build();

//Call this method from within your activity passing the context
NavigationLauncher.startNavigation(YourActivity.this, options);
```

##### Customizing navigation component

Include the instructionview and summary view so, that they can be customized in the navigation layout.
```

<com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView
        android:id="@+id/instructionView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingBottom="80dp"
        android:visibility="invisible"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/summaryBottomSheet"/>
    <com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet
        android:id="@+id/summaryBottomSheet"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        android:visibility="invisible"/>
        
 ```

###### Listening to progress change

Like tracking user location changes, the ProgressChangeListener is invoked every time the user's location changes and provides an updated RouteProgress object. The Baato Navigation UI SDK uses this listener by default, but if you are not using the Baato Navigation UI SDK, it is strongly encouraged that you also use this listener. The ProgressChangeListener can typically be used to refresh most of your application's user interface when a change occurs. For example, if you are displaying the user's current progress until the user needs to do the next maneuver. Every time this listener's invoked, you can update your view with the new information from RouteProgress.

Besides receiving information about the route progress, the callback also provides you with the user's current location, which can provide their current speed, bearing, etc. If you have the snap-to-route enabled, the location object will be updated to give the snapped coordinates.

```
// Implement ProgressChangeListener and add on navigation ready

navigation.addProgressChangeListener(this);

// Update the instructions on progress changed

@Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    // Cache "snapped" Locations for re-route Directions API requests
    updateLocation(location);

    // Update InstructionView data from RouteProgress
    instructionView.update(routeProgress);
    summaryBottomSheet.update(routeProgress);
  }

```

Updating the camera to the new location on progress changed.
```
CameraPosition cameraPosition = buildCameraPositionFrom(location, location.getBearing());
    navigationMap.retrieveMap().animateCamera(
      CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
    );
```

Add offroutelistner for offroute detection and compute route
```

@Override
  public void userOffRoute(Location location) {
    calculateRouteWith(destination, true);
    // request new route with the origin as a current position
    // origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()); where last location can be obtained from locationengine

@Override
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }
  
  // handle new route
  private void handleRoute(DirectionsResponse response, boolean isOffRoute) {
    List<DirectionsRoute> routes = response.routes();
    if (!routes.isEmpty()) {
      route = routes.get(FIRST);
      navigationMap.drawRoute(route);
      if (isOffRoute) {
        navigation.startNavigation(route);
      }
    }
  }
  
```

###### Customizing instruction view

Add a custom style in style.xml and add it as a theme on create of navigation activity

```
<style name="CustomNavigationMapRoute" parent="NavigationMapRoute">
        <item name="upcomingManeuverArrowBorderColor"></item>
    </style>

    <style name="CustomNavigationView" parent="NavigationViewLight">
        <item name="navigationViewRouteStyle">@color/colorAccent</item>
    </style>

    <style name="CustomInstructionView" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="navigationViewPrimary">@color/mapbox_navigation_view_color_primary</item>
        <item name="navigationViewSecondary">@color/mapbox_navigation_view_color_secondary</item>
        <item name="navigationViewAccent">@color/mapbox_navigation_view_color_accent</item>
        <item name="navigationViewPrimaryText">@color/mapbox_navigation_view_color_secondary</item>
        <item name="navigationViewSecondaryText">@color/mapbox_navigation_view_color_accent_text</item>
        <item name="navigationViewDivider">@color/mapbox_navigation_view_color_divider</item>

        <item name="navigationViewListBackground">@color/mapbox_navigation_view_color_list_background</item>

        <item name="navigationViewBannerBackground">@color/mapbox_navigation_view_color_banner_background</item>
        <item name="navigationViewBannerPrimaryText">@color/mapbox_navigation_view_color_banner_primary_text</item>
        <item name="navigationViewBannerSecondaryText">@color/mapbox_navigation_view_color_banner_secondary_text</item>
        <item name="navigationViewBannerManeuverPrimary">@color/mapbox_navigation_view_color_banner_maneuver_primary</item>
        <item name="navigationViewBannerManeuverSecondary">@color/mapbox_navigation_view_color_banner_maneuver_secondary</item>

        <item name="navigationViewProgress">@color/mapbox_navigation_view_color_progress</item>
        <item name="navigationViewProgressBackground">@color/mapbox_navigation_view_color_progress_background</item>

        <item name="navigationViewRouteStyle">@style/NavigationMapRoute</item>

        <item name="navigationViewLocationLayerStyle">@style/mapbox_LocationLayer</item>

        <item name="navigationViewDestinationMarker">@drawable/map_marker_light</item>

        <item name="navigationViewRouteOverviewDrawable">@drawable/ic_route_preview</item>

        <item name="navigationViewMapStyle">@string/navigation_guidance_day</item>
    </style>
   
 ```
 Add theme to activity on create method
 
```
setTheme(R.style.CustomInstructionView);
```

##### Testing with the mock location activity

Implement NavigationEventListner and make a NavigationOptions as

```

MapboxNavigationOptions options = MapboxNavigationOptions.builder()
                .build();
        navigation = new MapboxNavigation(this, "pk.xxx", options);
        navigation.addMilestone(new RouteMilestone.Builder()
                .setIdentifier(BEGIN_ROUTE_MILESTONE)
                .setInstruction(new BeginRouteInstruction())
                .setTrigger(
                        Trigger.all(
                                Trigger.lt(TriggerProperty.STEP_INDEX, 3),
                                Trigger.gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 200),
                                Trigger.gte(TriggerProperty.STEP_DISTANCE_TRAVELED_METERS, 75)
                        )
                ).build());
 ```
 
 Add location layer render mode and replay engine
 
 ``` 
 locationLayerPlugin.setRenderMode(RenderMode.GPS);
                locationLayerPlugin.setLocationLayerEnabled(false);
                navigationMapRoute = new NavigationMapRoute(navigation, mapView, mapboxMap);
                navigationMapRoute.addRoute(route);
                locationEngine = new ReplayRouteLocationEngine();
 ```
 
 After navigation is ready
 
 ```
  // Attach all of our navigation listeners.
            navigation.addNavigationEventListener(this);
            navigation.addProgressChangeListener(this);
            navigation.addMilestoneEventListener(this);
            navigation.addOffRouteListener(this);
            
  // Attach the replay engine
  ((ReplayRouteLocationEngine) locationEngine).assign(route);
            navigation.setLocationEngine(locationEngine);
            locationLayerPlugin.setLocationLayerEnabled(true);
  
  // Start navigation
            navigation.startNavigation(route);
  ```
  
  Force update the location and instructions on progress changed
  
  ```
  @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {

        // Cache "snapped" Locations for re-route Directions API requests

            if (location == null) {
                location = lastLocation;
            }
            locationLayerPlugin.forceLocationUpdate(location);
            moveCameraTo(location);
            // Update InstructionView data from RouteProgress
            instructionView.update(routeProgress);
            summaryBottomSheet.update(routeProgress);
    }
 ```

For customizing the navigation you can refer the app; which have different use case scenarios

## Built With

* [Retrofit](https://github.com/square/retrofit) - Used to handle API requests
* [Maven](https://maven.apache.org/) - Dependency Management
