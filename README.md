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

#### If your targetVersion includes Android 9 and above,

 Add the following permission to the Manifest file in your Android project
```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### Implementation

#### Request a route

You can reuest a route using BaatoNavigationRoute. Follow the implementation details from [Baato-Java Client](https://github.com/baato/java-client). 
Once you request a route from the baato java-client, you are ready to use Navigation UI SDK.

#### Turn by Turn navigation

You can launch the UI using Navigation launcher with current route received from [Baato-Java Client](https://github.com/baato/java-client) 

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

## Built With

* [Retrofit](https://github.com/square/retrofit) - Used to handle API requests
* [Maven](https://maven.apache.org/) - Dependency Management
