package com.bhawak.osmnavigation.navigation.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.bhawak.osmnavigation.MainActivity;
import com.bhawak.osmnavigation.R;
import com.bhawak.osmnavigation.navigation.DistanceConfig;
import com.bhawak.osmnavigation.navigation.DistanceUtils;
import com.bhawak.osmnavigation.navigation.NavResponse;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverter;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverterTranslationMap;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.graphhopper.util.TranslationMap;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.FeedbackButton;
import com.mapbox.services.android.navigation.ui.v5.camera.DynamicCamera;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.util.List;
import java.util.Locale;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ComponentNavigationActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapLongClickListener, LocationEngineListener, ProgressChangeListener,
        MilestoneEventListener, OffRouteListener {

  private static final int FIRST = 0;
  private static final int ONE_HUNDRED_MILLISECONDS = 100;
  private static final int BOTTOMSHEET_PADDING_MULTIPLIER = 4;
  private static final int TWO_SECONDS_IN_MILLISECONDS = 2000;
  private static final double BEARING_TOLERANCE = 90d;
  private static final String LONG_PRESS_MAP_MESSAGE = "Long press the map to select a destination.";
  private static final String SEARCHING_FOR_GPS_MESSAGE = "Searching for GPS...";
  private static final int ZERO_PADDING = 0;
  private static final double DEFAULT_ZOOM = 12.0;
  private static final double DEFAULT_TILT = 0d;
  private static final double DEFAULT_BEARING = 0d;
  private static final int ONE_SECOND_INTERVAL = 1000;

  @BindView(R.id.componentNavigationLayout)
  ConstraintLayout navigationLayout;

  @BindView(R.id.mapView)
  MapView mapView;

  @BindView(R.id.instructionView)
  InstructionView instructionView;

//  @BindView(R.id.feedbackFab)
//  FloatingActionButton feedbackFab;

  @BindView(R.id.startNavigationFab)
  FloatingActionButton startNavigationFab;

  @BindView(R.id.summaryBottomSheet)
  SummaryBottomSheet summaryBottomSheet;

  @BindView(R.id.cancelNavigationFab)
  FloatingActionButton cancelNavigationFab;

  private LocationEngine locationEngine;
  private LocationLayerPlugin locationLayerPlugin;
  private MapboxNavigation navigation;
  private NavigationSpeechPlayer speechPlayer;
  private NavigationMapboxMap navigationMap;
  private Location lastLocation;
  private DirectionsRoute route;
  private Point destination;
  private MapState mapState;
  private MapboxMap mapboxMap;

//  @Override
//  public void onRunning(boolean running) {
//    if (running) {
//      Timber.d("onRunning: Started");
//    } else {
//      Timber.d("onRunning: Stopped");
//    }
//  }


  private enum MapState {
    INFO,
    NAVIGATION
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // For styling the InstructionView
    setTheme(R.style.CustomInstructionView);
    setContentView(R.layout.activity_component_navigation);
    Mapbox.getInstance(getApplicationContext(), "pk.xxx");
    ButterKnife.bind(this);
    mapView.onCreate(savedInstanceState);

    // Will call onMapReady
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyleUrl("http://178.128.59.143:8080/api/v2/styles/a1e37ae99cdb4f29910cdf27a51a0282.json", new MapboxMap.OnStyleLoadedListener() {
      @Override
      public void onStyleLoaded(@NonNull String style) {
//    mapboxMap.setStyleUrl("http://178.128.59.143:8080/api/v2/styles/a1e37ae99cdb4f29910cdf27a51a0282.json");
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(27.7172, 85.3240))
                .zoom(12)
                .build());
        mapState = MapState.INFO;
        navigationMap = new NavigationMapboxMap(mapView, mapboxMap);
        // For voice instructions
        initializeSpeechPlayer();

        // For Location updates
        initializeLocationEngine();

        // For navigation logic / processing
        initializeNavigation(mapboxMap);
      }
    });

  }

  @Override
  public void onMapLongClick(@NonNull LatLng point) {
    // Only reverse geocode while we are not in navigation
    if (mapState.equals(MapState.NAVIGATION)) {
      return;
    }

    // Fetch the route with this given point
    destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
    calculateRouteWith(destination, false);

    // Clear any existing markers and add new one
    navigationMap.clearMarkers();
    navigationMap.addMarker(this, destination);

    // Update camera to new destination
    moveCameraToInclude(destination);
//    vibrate();
  }

  @OnClick(R.id.startNavigationFab)
  public void onStartNavigationClick(FloatingActionButton floatingActionButton) {
    // Transition to navigation state
    mapState = MapState.NAVIGATION;

    floatingActionButton.hide();
    cancelNavigationFab.show();

    // Show the InstructionView
    TransitionManager.beginDelayedTransition(navigationLayout);
    instructionView.setVisibility(View.VISIBLE);
    summaryBottomSheet.setVisibility(View.VISIBLE);
//    feedbackFab.setVisibility(View.GONE);

    // Start navigation
    adjustMapPaddingForNavigation();

    //Replay
    // Attach all of our navigation listeners.
//    locationEngine = new ReplayRouteLocationEngine();
//    navigation.addNavigationEventListener(this);
//    navigation.addProgressChangeListener(this);
//    navigation.addMilestoneEventListener(this);
//    navigation.addOffRouteListener(this);
//    ((ReplayRouteLocationEngine) locationEngine).assign(route);
//    navigation.setLocationEngine(locationEngine);
//    locationLayerPlugin.setLocationLayerEnabled(true);
//    mapView.findViewById(R.id.feedbackFab).setVisibility(View.GONE);
    navigation.startNavigation(route);
    // Location updates will be received from ProgressChangeListener
    removeLocationEngineListener();
  }

  @OnClick(R.id.cancelNavigationFab)
  public void onCancelNavigationClick(FloatingActionButton floatingActionButton) {
    // Transition to info state
    mapState = MapState.INFO;

    floatingActionButton.hide();

    // Hide the InstructionView
    TransitionManager.beginDelayedTransition(navigationLayout);
    instructionView.setVisibility(View.INVISIBLE);
    summaryBottomSheet.setVisibility(View.INVISIBLE);

    // Reset map camera and pitch
    resetMapAfterNavigation();

    // Add back regular location listener
    addLocationEngineListener();
  }

  /*
   * LocationEngine listeners
   */

  @SuppressLint("MissingPermission")
  @Override
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    if (lastLocation == null) {
      // Move the navigationMap camera to the first Location
      moveCameraTo(location);

      // Allow navigationMap clicks now that we have the current Location
      navigationMap.retrieveMap().addOnMapLongClickListener(this);
      showSnackbar(LONG_PRESS_MAP_MESSAGE, BaseTransientBottomBar.LENGTH_LONG);
    }

    // Cache for fetching the route later
    updateLocation(location);
  }

  /*
   * Navigation listeners
   */

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    // Cache "snapped" Locations for re-route Directions API requests
    updateLocation(location);

    // Update InstructionView data from RouteProgress
    instructionView.update(routeProgress);
    summaryBottomSheet.update(routeProgress);
  }

  @Override
  public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
    playAnnouncement(milestone);
  }

  @Override
  public void userOffRoute(Location location) {
    calculateRouteWith(destination, true);
  }

  /*
   * Activity lifecycle methods
   */

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

  private void initializeSpeechPlayer() {
    String english = Locale.US.getLanguage();
    String accessToken = Mapbox.getAccessToken();
//      String accessToken = "pk.xxx";
    SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(getApplication(), english, true, accessToken);
    speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
  }

  private void initializeLocationEngine() {
    LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
    locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.LOW_POWER);
    locationEngine.addLocationEngineListener(this);
    locationEngine.setFastestInterval(ONE_SECOND_INTERVAL);
    locationEngine.activate();
    showSnackbar(SEARCHING_FOR_GPS_MESSAGE, BaseTransientBottomBar.LENGTH_SHORT);
  }

  private void initializeNavigation(MapboxMap mapboxMap) {
    navigation = new MapboxNavigation(this, "pk.xxx");
    navigation.setLocationEngine(locationEngine);
    navigation.setCameraEngine(new DynamicCamera(mapboxMap));
    navigation.addProgressChangeListener(this);
    navigation.addMilestoneEventListener(this);
    navigation.addOffRouteListener(this);
    navigationMap.addProgressChangeListener(navigation);

    //    locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap);
    locationLayerPlugin.setRenderMode(RenderMode.GPS);
    locationLayerPlugin.setLocationLayerEnabled(false);
  }

  private void showSnackbar(String text, int duration) {
    Snackbar.make(navigationLayout, text, duration).show();
  }

  private void playAnnouncement(Milestone milestone) {
    if (milestone instanceof VoiceInstructionMilestone) {
      SpeechAnnouncement announcement = SpeechAnnouncement.builder()
        .voiceInstructionMilestone((VoiceInstructionMilestone) milestone)
        .build();
      speechPlayer.play(announcement);
    }
  }

  private void updateLocation(Location location) {
    lastLocation = location;
    navigationMap.updateLocation(location);
  }

  private void moveCameraTo(Location location) {
    CameraPosition cameraPosition = buildCameraPositionFrom(location, location.getBearing());
    navigationMap.retrieveMap().animateCamera(
      CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
    );
  }

  private void moveCameraToInclude(Point destination) {
    LatLng origin = new LatLng(lastLocation);
    LatLngBounds bounds = new LatLngBounds.Builder()
      .include(origin)
      .include(new LatLng(destination.latitude(), destination.longitude()))
      .build();
    Resources resources = getResources();
    int routeCameraPadding = (int) resources.getDimension(R.dimen.component_navigation_route_camera_padding);
    int[] padding = {routeCameraPadding, routeCameraPadding, routeCameraPadding, routeCameraPadding};
    CameraPosition cameraPosition = navigationMap.retrieveMap().getCameraForLatLngBounds(bounds, padding);
    navigationMap.retrieveMap().animateCamera(
      CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
    );
  }

  private void moveCameraOverhead() {
    if (lastLocation == null) {
      return;
    }
    CameraPosition cameraPosition = buildCameraPositionFrom(lastLocation, DEFAULT_BEARING);
    navigationMap.retrieveMap().animateCamera(
      CameraUpdateFactory.newCameraPosition(cameraPosition), TWO_SECONDS_IN_MILLISECONDS
    );
  }

  @NonNull
  private CameraPosition buildCameraPositionFrom(Location location, double bearing) {
    return new CameraPosition.Builder()
      .zoom(DEFAULT_ZOOM)
      .target(new LatLng(location.getLatitude(), location.getLongitude()))
      .bearing(bearing)
      .tilt(DEFAULT_TILT)
      .build();
  }

  private void adjustMapPaddingForNavigation() {
    Resources resources = getResources();
    int mapViewHeight = mapView.getHeight();
    int bottomSheetHeight = (int) resources.getDimension(R.dimen.component_navigation_bottomsheet_height);
    int topPadding = mapViewHeight - (bottomSheetHeight * BOTTOMSHEET_PADDING_MULTIPLIER);
    navigationMap.retrieveMap().setPadding(ZERO_PADDING, topPadding, ZERO_PADDING, ZERO_PADDING);
  }

  private void resetMapAfterNavigation() {
    navigationMap.removeRoute();
    navigationMap.clearMarkers();
    navigation.stopNavigation();
    moveCameraOverhead();
  }

  private void removeLocationEngineListener() {
    if (locationEngine != null) {
      locationEngine.removeLocationEngineListener(this);
    }
  }

  private void addLocationEngineListener() {
    if (locationEngine != null) {
      locationEngine.addLocationEngineListener(this);
    }
  }

  private void calculateRouteWith(Point destination, boolean isOffRoute) {
    Point origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
    Double bearing = Float.valueOf(lastLocation.getBearing()).doubleValue();
    getRoute(origin, destination, isOffRoute);

//    NavigationRoute.builder(this)
//      .accessToken(Mapbox.getAccessToken())
//      .origin(origin, bearing, BEARING_TOLERANCE)
//      .destination(destination)
//      .build()
//      .getRoute(new Callback<DirectionsResponse>() {
//        @Override
//        public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
//          handleRoute(response, isOffRoute);
//        }
//
//        @Override
//        public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
//          Timber.e(throwable);
//        }
//      });
  }
  private void getRoute(Point origin, Point destination, boolean isOffRoute) {
    String[] points = new String[2];
    points[0] = origin.latitude() + "," + origin.longitude();
//        points[0] = "27.713042695157757,85.2703857421875";
    points[1] = destination.latitude() + "," + destination.longitude();
    Call<NavResponse> call = MainActivity.getApiInterface().getRoutes(points, "car", false);
    call.enqueue(new Callback<NavResponse>() {
      @Override
      public void onResponse(Call<NavResponse> call, Response<NavResponse> response) {

        if (response.body() == null) {
//          Log.e(TAG, "No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().getInstructionList() != null && response.body().getInstructionList().size() == 0 ) {
//          Log.e(TAG, "No routes found");
          return;
        }
//        Timber.wtf("Anno: " + String.valueOf(response.body().getPath().getInstructions().get(0).getAnnotation()));
//        encodedPolyline = response.body().getEncoded_polyline();
//        initRouteCoordinates();
        TranslationMap translationMap = null;
        final TranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap().doImport();
        ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(response.body(), Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, translationMap, navigateResponseConverterTranslationMap, Locale.ENGLISH));
//                Timber.d( "MapObj" + obj);
//                Log.d(TAG, "onResponse: " + response.body().toString());
//                Timber.d(response.body().toString());
        DirectionsResponse directionsResponse = DirectionsResponse.fromJson(obj.toString());

        route = directionsResponse.routes().get(0);
//                try {
//                    currentRoute = DirectionsResponse.fromJson(returnFromRaw()).routes().get(0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
          handleRoute(directionsResponse, isOffRoute);
//        navMapRoute(currentRoute);

      }

      @Override
      public void onFailure(Call<NavResponse> call, Throwable t) {

      }
    });
  }

  private void handleRoute(DirectionsResponse response, boolean isOffRoute) {
    List<DirectionsRoute> routes = response.routes();
    if (!routes.isEmpty()) {
      route = routes.get(FIRST);
      navigationMap.drawRoute(route);
      if (isOffRoute) {
        navigation.startNavigation(route);
      } else {
        startNavigationFab.show();
      }
    }
  }

  @SuppressLint("MissingPermission")
  private void vibrate() {
    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(ONE_HUNDRED_MILLISECONDS, VibrationEffect.DEFAULT_AMPLITUDE));
    } else {
      vibrator.vibrate(ONE_HUNDRED_MILLISECONDS);
    }
  }

}
