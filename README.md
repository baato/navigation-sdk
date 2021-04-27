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

// This sdk usage phone state permission for telemetry service (i.e, it use cellular network and other connectivity status to compute gps for navigation. This permission must be explicitly added to the code for android 11 and above.) 
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

### Implementation

#### Managing the location permission and location update (add phone state permission for android 11 and above)

1. Implement the PermissionsListener, for navigation to work with better experience you need to provide the runtime location permission
```
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
     // here Constants.PHONE_STATE_PERMISSION_REQUEST is a static integer can be also be defined in seperate interface/class.                                  
     if (requestCode == Constants.PHONE_STATE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                displayPhoneStateRequiredDialog(this);
            } else
                onStartNavigation(); // Use this method to start navigation (The method used here can be method that triggers intent if navigation is in different activity)
        } else                                      
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
    
    // Example asking phone state permission you can implement your own method
     public static void displayPhoneStateRequiredDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Turn by turn navigation task requires PHONE_STATE_PERMISSION enabled. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
```    
2. Use permission manager to check if location permission is granted. Also check phone state permission before launching navigation.
```
// For location 
       permissionsManager = new PermissionsManager(context);
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            permissionsManager.requestLocationPermissions(context);
        } else {
        // get location and set map component accordingly
            getMyLocation();
        }
        
 // For phone state (call this function just before starting navigation) 
 private void checkPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int res = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, Constants.PHONE_STATE_PERMISSION_REQUEST);
            } else onStartNavigation(); // Use this method to start navigation.
        }
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

###### Adding Instruction and summary layout 

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
 
 ###### Implement the event listner and override methods
 
The customizable navigation activity class must implement ProgressChangeListener, MilestoneEventListener and OffRouteListener for tracking and updating navigation component. You also need some the components which are:

1. **MapboxNavigation** class instances is used to setup, customize, start, and end a navigation session.
2. **NavigationMapboxMap** class instances initializes various map-related components and plugins that are
   useful for providing a navigation-driven map experience.
 *These APIs include drawing a route line, camera animations, and more.*
3. **NavigationSpeechPlayer** provide voice API.

Setting up navigation activity

 ```
  public class CustomNavigationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,       ProgressChangeListener, MilestoneEventListener, OffRouteListener {
        // override the methods where necessary
        
         private MapboxNavigation navigation;
         private NavigationSpeechPlayer speechPlayer;
         private NavigationMapboxMap navigationMap;
         
         @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // For styling the InstructionView
    setTheme(R.style.CustomInstructionView);
    setContentView(R.layout.custom_navigation);
    
    //Initializing mapbox, if already initialized you can use "pk.xxx" since we are using baato service. 
    Mapbox.getInstance(getApplicationContext(), mappbox_token);
    
     mapView.onCreate(savedInstanceState);

    // Will call onMapReady
    mapView.getMapAsync(this);

   // prepare navigation option for MapboxNavigation. 
    MapboxNavigationOptions options = MapboxNavigationOptions.builder()
            .defaultMilestonesEnabled(true)
            .build();
    navigation = new MapboxNavigation(this, "pk.xxx", options);
  }
  
  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    //remove mapbox attribute
    mapboxMap.getUiSettings().setAttributionEnabled(false);
    mapboxMap.getUiSettings().setLogoEnabled(false);
    
    mapboxMap.setStyleUrl("http://baato.io/api/v1/styles/retro?key=" + baato_token, new   MapboxMap.OnStyleLoadedListener()     {
      @Override
      public void onStyleLoaded(@NonNull String style) {
      
      //moving map center to gps location with zoom level 14
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(origin.latitude(), origin.longitude()))
                .zoom(14)
                .build());
       
        navigationMap = new NavigationMapboxMap(mapView, mapboxMap);
        // For voice instructions
        initializeSpeechPlayer();

        // For Location updates
        initializeLocationEngine();

        // For navigation logic / processing
        initializeNavigation(mapboxMap);
        
        //handle mapevent and compute route accordingly
      }
    });
    
    // For voice instructions
     private void initializeSpeechPlayer() {
      String english = Locale.US.getLanguage();
      String accessToken = "pk.xxx";
      SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(getApplication(), english, true, accessToken);
      speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
    }
    
   // For Location updates
   private void initializeLocationEngine() {
    LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
    locationEngine = locationEngineProvider.obtainLocationEngineBy(LocationEngine.Type.ANDROID);
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.addLocationEngineListener(this);
    locationEngine.setFastestInterval(ONE_SECOND_INTERVAL);
    locationEngine.activate();
  }
  
  // For navigation logic / processing
  private void initializeNavigation(MapboxMap mapboxMap) {
    navigation.setLocationEngine(locationEngine);
    navigation.setCameraEngine(new DynamicCamera(mapboxMap));
    navigation.addProgressChangeListener(this);
    navigation.addMilestoneEventListener(this);
    navigation.addOffRouteListener(this);
    navigationMap.addProgressChangeListener(navigation);
  }
  
  //After computing route start navigation like this method
  public void onStartNavigation() {
    
    // Show the InstructionView
    TransitionManager.beginDelayedTransition(navigationLayout);
    instructionView.setVisibility(View.VISIBLE);
    summaryBottomSheet.setVisibility(View.VISIBLE);

    // Start navigation
    adjustMapPaddingForNavigation();
    navigation.startNavigation(route);
    
    //  attach soundButton to handle its event  
    soundButton = instructionView.findViewById(R.id.soundLayout);
    soundButton.addOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        isMuted = soundButton.toggleMute();
      }
    });
    // Location updates will be received from ProgressChangeListener
    removeLocationEngineListener();
  }
  
  // Adjustig map padding for navigation
  private void adjustMapPaddingForNavigation() {
    int mapViewHeight = mapView.getHeight();
    int bottomSheetHeight = summaryBottomSheet.getHeight();
    int topPadding = mapViewHeight - (bottomSheetHeight * BOTTOMSHEET_PADDING_MULTIPLIER);
    navigationMap.retrieveMap().setPadding(ZERO_PADDING, topPadding, ZERO_PADDING, ZERO_PADDING);
  }

  // Reset navigation view
  public void onCancelNavigation() {
   // Reset view
    TransitionManager.beginDelayedTransition(navigationLayout);
    instructionView.setVisibility(View.INVISIBLE);
    summaryBottomSheet.setVisibility(View.INVISIBLE);

    // Reset map camera and pitch
    resetMapAfterNavigation();

    // Add back regular location listener
    addLocationEngineListener();
  }
```
###### Listening to progress change

Like tracking user location changes, the ProgressChangeListener is invoked every time the user's location changes and provides an updated RouteProgress object. The ProgressChangeListener can typically be used to refresh most of your application's user interface when a change occurs. For example, if you are displaying the user's current progress until the user needs to do the next maneuver. Every time this listener's invoked, you can update your view with the new information from RouteProgress.

Besides receiving information about the route progress, the callback also provides you with the user's current location, which can provide their current speed, bearing, etc. If you have the snap-to-route enabled, the location object will be updated to give the snapped coordinates.

```
// Implement ProgressChangeListener and add on navigation ready which is also shown in initializeNavigation method

navigation.addProgressChangeListener(this);

// Update the instructions on progress changed

@Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    // Cache "snapped" Locations for re-route Directions API requests
    navigationMap.updateLocation(location);

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
    
 // building camera position
  private CameraPosition buildCameraPositionFrom(Location location, double bearing) {
    return new CameraPosition.Builder()
      .zoom(DEFAULT_ZOOM)
      .target(new LatLng(location.getLatitude(), location.getLongitude()))
      .bearing(bearing)
      .tilt(DEFAULT_TILT)
      .build();
  }
```

Add offroutelistner for offroute detection and compute route
```

@Override
  public void userOffRoute(Location location) {
    // request new route with the origin as a current position
     origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()); where last location can be      obtained from locationengine 
    // getRoute method is explained in the baato-java client doc
    DirectionsAPIResponse directionResponse = getRoute(origin, destination);
     handleRoute(directionsResponse, isOffRoute);
    }
    
    
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
Adding Voice intruction on milestone event
```
  @Override
  public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
    playAnnouncement(milestone);
  }
  
  private void playAnnouncement(Milestone milestone) {
  //check weather mute button is active or not
    if(!isMuted) {
      if (milestone instanceof VoiceInstructionMilestone) {
        SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone((VoiceInstructionMilestone) milestone)
                .build();
        Log.d("Announcement", announcement.toString());
        speechPlayer.play(announcement);
      }
    }
  }
```
Handling activity lifecycle method
```
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    if (navigationMap != null) {
      navigationMap.onStart();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    if (navigationMap != null) {
      navigationMap.onStop();
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();

    // Ensure proper shutdown of the SpeechPlayer
    if (speechPlayer != null) {
      speechPlayer.onDestroy();
    }

    // Prevent leaks
    removeLocationEngineListener();

    ((DynamicCamera) navigation.getCameraEngine()).clearMap();
    // MapboxNavigation will shutdown the LocationEngine
    navigation.onDestroy();
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

Mock Location Activity uses ReplayRouteLocationEngine which helps to mock the user location along the route. This helps for calibrating the test use-case for developers. You can also use the Custom Navigation as above with some gps mocking third-party application like Lockito. 

This activity needs to implement OnMapReadyCallback, ProgressChangeListener, MilestoneEventListener, OffRouteListener, NavigationEventListener, SpeechAnnouncementListener so as to recive all the updates. Implementation are same as above.

Implement NavigationEventListner and make a NavigationOptions as below.   

```
@Override
  public void onRunning(boolean running) {
  // do something
  }
  
MapboxNavigationOptions options = MapboxNavigationOptions.builder()
                .build();
        navigation = new MapboxNavigation(this, "pk.xxx", options);
        
        // you can also add custom milestone
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
