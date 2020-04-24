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
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### Implementation

#### You have to add location permission and location change listner for the location update

1. Implement the PermissionsListener, since navigation to work you need to give the runtime location permission
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
        }
```        
3. Implement the LocationEngineListener for location updates when map is ready
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
4. Deactivate location engine on destroy method to prevent the memory leak
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

For customizing the navigation you can refeer the App; which have different use case scenarios

## Built With

* [Retrofit](https://github.com/square/retrofit) - Used to handle API requests
* [Maven](https://maven.apache.org/) - Dependency Management
