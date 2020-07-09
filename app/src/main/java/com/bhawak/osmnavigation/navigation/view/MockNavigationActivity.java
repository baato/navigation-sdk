package com.bhawak.osmnavigation.navigation.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.baato.baatolibrary.models.DirectionsAPIResponse;
import com.baato.baatolibrary.models.NavResponse;
import com.baato.baatolibrary.services.BaatoRouting;
import com.bhawak.osmnavigation.R;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverter;
import com.bhawak.osmnavigation.navigation.view.notification.CustomNavigationNotification;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import com.mapbox.services.android.navigation.ui.v5.NavigationButton;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;
import com.mapbox.services.android.navigation.ui.v5.SoundButton;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement;
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayerProvider;
import com.mapbox.services.android.navigation.v5.instruction.Instruction;
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.milestone.RouteMilestone;
import com.mapbox.services.android.navigation.v5.milestone.Trigger;
import com.mapbox.services.android.navigation.v5.milestone.TriggerProperty;
import com.mapbox.services.android.navigation.v5.milestone.VoiceInstructionMilestone;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MockNavigationActivity extends AppCompatActivity implements OnMapReadyCallback, ProgressChangeListener,
        MilestoneEventListener, OffRouteListener, NavigationEventListener, SpeechAnnouncementListener {

  private static final int FIRST = 0;
  private static final int ONE_HUNDRED_MILLISECONDS = 100;
  private static final int BOTTOMSHEET_PADDING_MULTIPLIER = 4;
  private static final int TWO_SECONDS_IN_MILLISECONDS = 2000;
  private static final double BEARING_TOLERANCE = 90d;
  private static final String LONG_PRESS_MAP_MESSAGE = "Long press the map to select a destination.";
  private static final String SEARCHING_FOR_GPS_MESSAGE = "Searching for GPS...";
  private static final int ZERO_PADDING = 0;
  private static final double DEFAULT_ZOOM = 14.0;
  private static final double DEFAULT_TILT = 0d;
  private static final double DEFAULT_BEARING = 0d;
  private static final int ONE_SECOND_INTERVAL = 1000;
  private static final int BEGIN_ROUTE_MILESTONE = 1001;

  @BindView(R.id.componentNavigationLayout)
  ConstraintLayout navigationLayout;

  @BindView(R.id.mapView)
  MapView mapView;

  @BindView(R.id.instructionView)
  InstructionView instructionView;

  //  @BindView(R.id.feedbackFab)
//  FloatingActionButton feedbackFab;
  @BindView(R.id.summaryBottomSheet)
  SummaryBottomSheet summaryBottomSheet;

  private LocationEngine locationEngine;
  private LocationLayerPlugin locationLayerPlugin;
  private MapboxNavigation navigation;
  private NavigationSpeechPlayer speechPlayer;
  private NavigationMapboxMap navigationMap;
  private Location lastLocation;
  private DirectionsRoute route;
  private Point destination;
  private MockNavigationActivity.MapState mapState;
  private MapboxMap mapboxMap;
  private String navigationMode = "car";
  private Point origin;
  DirectionsResponse directionsResponse;
  private NavigationMapRoute navigationMapRoute;
  private SoundButton soundButton;
  private boolean isMuted = false;

  @Override
  public void onRunning(boolean running) {
  Log.wtf("Status: ", String.valueOf(running));
  }

  @Override
  public String willVoice(SpeechAnnouncement announcement) {
    return null;
  }

  private static class MyBroadcastReceiver extends BroadcastReceiver {
    private final WeakReference<MapboxNavigation> weakNavigation;

    MyBroadcastReceiver(MapboxNavigation navigation) {
      this.weakNavigation = new WeakReference<>(navigation);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      MapboxNavigation navigation = weakNavigation.get();
      navigation.stopNavigation();
    }
  }


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
    Mapbox.getInstance(getApplicationContext(), null);
    ButterKnife.bind(this);
    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      directionsResponse = (DirectionsResponse) extras.get("Route");
      route = directionsResponse.routes().get(0);
      origin = (Point) extras.get("origin");
      lastLocation = (Location) extras.get("lastLocation");
      // and get whatever type user account id is
    }
    mapView.setStyleUrl("http://api.baato.io/api/v1/styles/retro?key=" + Constants.token);
    mapView.onCreate(savedInstanceState);

    // Will call onMapReady
    mapView.getMapAsync(this);
        CustomNavigationNotification customNotification = new CustomNavigationNotification(getApplicationContext());
//        .navigationNotification(customNotification)
    MapboxNavigationOptions options = MapboxNavigationOptions.builder()
            .navigationNotification(customNotification)
            .defaultMilestonesEnabled(true)
            .build();
    navigation = new MapboxNavigation(this, Constants.token, options);

    navigation.addMilestone(new RouteMilestone.Builder()
            .setIdentifier(BEGIN_ROUTE_MILESTONE)
            .setInstruction(new BeginRouteInstruction())
            .setTrigger(Trigger.all())
            .build());
//    navigation.addMilestone(new RouteMilestone.Builder()
//            .setIdentifier(BEGIN_ROUTE_MILESTONE)
//            .setInstruction(new BeginRouteInstruction())
//            .setTrigger(
//                    Trigger.all(
//                            Trigger.lt(TriggerProperty.STEP_INDEX, 3),
//                            Trigger.gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 200),
//                            Trigger.gte(TriggerProperty.STEP_DISTANCE_TRAVELED_METERS, 5)
//                    )
//            ).build());
        customNotification.register(new MyBroadcastReceiver(navigation), getApplicationContext());
  }

  private static class BeginRouteInstruction extends Instruction {

    @Override
    public String buildInstruction(RouteProgress routeProgress) {
      return "Have a safe trip!";
    }
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    //remove mapbox attribute
    mapboxMap.getUiSettings().setAttributionEnabled(false);
    mapboxMap.getUiSettings().setLogoEnabled(false);
    mapboxMap.setStyleUrl("http://api.baato.io/api/v1/styles/retro?key=" + Constants.token, new MapboxMap.OnStyleLoadedListener() {
      //        mapboxMap.setStyle(Style.MAPBOX_STREETS, new MapboxMap.OnStyleLoadedListener() {
      @Override
      public void onStyleLoaded(@NonNull String style) {
//    mapboxMap.setStyleUrl("http://178.128.59.143:8080/api/v2/styles/a1e37ae99cdb4f29910cdf27a51a0282.json");
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(origin.latitude(), origin.longitude()))
                .zoom(14)
                .build());
        initializeSpeechPlayer();
        mapState = MockNavigationActivity.MapState.NAVIGATION;
        locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap);
        locationLayerPlugin.setRenderMode(RenderMode.GPS);
        locationLayerPlugin.setLocationLayerEnabled(false);
        navigationMapRoute = new NavigationMapRoute(navigation, mapView, mapboxMap);
        navigationMapRoute.addRoute(route);
        locationEngine = new ReplayRouteLocationEngine();
        onNavigationReady();
      }
    });

  }

  public void onNavigationReady() {
    if (navigation != null && route != null) {
      // Transition to navigation state
      TransitionManager.beginDelayedTransition(navigationLayout);
      instructionView.setVisibility(View.VISIBLE);
      summaryBottomSheet.setVisibility(View.VISIBLE);
      // Attach all of our navigation listeners.
      navigation.addNavigationEventListener(this);
      navigation.addProgressChangeListener(this);
      navigation.addOffRouteListener(this);
      navigation.addMilestoneEventListener(this);
//      navigation.addMilestoneEventListener(new MilestoneEventListener() {
//        @Override
//        public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
//          Log.d("milestone", instruction);
//        }
//      });
      ((ReplayRouteLocationEngine) locationEngine).assign(route);
//      ((ReplayRouteLocationEngine) locationEngine).updateSpeed(100);
      navigation.setLocationEngine(locationEngine);
      locationLayerPlugin.setLocationLayerEnabled(true);
      navigation.startNavigation(route);
      soundButton = instructionView.findViewById(R.id.soundLayout);
      soundButton.addOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          isMuted = soundButton.toggleMute();
        }
      });
    }
  }


  /*
   * Navigation listeners
   */

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
    Log.d("Progress", String.valueOf(routeProgress.currentStepPoints()));
    Log.d("Location", String.valueOf(location));
  }

  @Override
  public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
    Log.d("milestone", instruction);
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
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStart();
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
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStop();
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
//        mapView.onDestroy();

    // Ensure proper shutdown of the SpeechPlayer
    if (speechPlayer != null) {
      speechPlayer.onDestroy();
    }
    if (navigation != null) {
//            ((DynamicCamera) navigation.getCameraEngine()).clearMap();
      // MapboxNavigation will shutdown the LocationEngine
      navigation.onDestroy();
    }
    locationEngine.removeLocationUpdates();
    locationEngine.deactivate();
    mapView.onDestroy();
  }

  private void initializeSpeechPlayer() {
    String english = Locale.US.getLanguage();
//    String accessToken = Mapbox.getAccessToken();
//      String accessToken = "pk.xxx";
    SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(getApplication(), english, true, Constants.token);
    speechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);
//      speechPlayer.speak(text, TextToSpeech.QUEUE_ADD, null);
  }

  private void showSnackbar(String text, int duration) {
    Snackbar.make(navigationLayout, text, duration).show();
  }

  private void playAnnouncement(Milestone milestone) {
//      Log.d("Announcement", milestone.toString());
    if (!isMuted) {
      if (milestone instanceof VoiceInstructionMilestone) {
        SpeechAnnouncement announcement = SpeechAnnouncement.builder()
                .voiceInstructionMilestone((VoiceInstructionMilestone) milestone)
                .build();
        speechPlayer.play(announcement);
      }
    }
  }

  private void moveCameraTo(Location location) {
    CameraPosition cameraPosition = buildCameraPositionFrom(location, location.getBearing());
    mapboxMap.animateCamera(
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
    int routeCameraPadding = 56;
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
    int bottomSheetHeight = 96;
    int topPadding = mapViewHeight - (bottomSheetHeight * BOTTOMSHEET_PADDING_MULTIPLIER);
    navigationMap.retrieveMap().setPadding(ZERO_PADDING, topPadding, ZERO_PADDING, ZERO_PADDING);
  }

  private void resetMapAfterNavigation() {
    navigationMap.removeRoute();
    navigationMap.clearMarkers();
    navigation.stopNavigation();
    moveCameraOverhead();
  }


  private void calculateRouteWith(Point destination, boolean isOffRoute) {
    Point origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
    Double bearing = Float.valueOf(lastLocation.getBearing()).doubleValue();
    if (destination == null){
      return;
    }
    getRoute(origin, destination, isOffRoute);
  }

  private void getRoute(Point origin, Point destination, boolean isOffRoute) {
    String[] points = new String[2];
    points[0] = origin.latitude() + "," + origin.longitude();
    points[1] = destination.latitude() + "," + destination.longitude();
//    new BaatoRouting(this)
//            .setPoints(points)
//            .setAccessToken(Constants.token)
//            .setMode(navigationMode) //eg bike, car, foot
//            .setAlternatives(false) //optional parameter
//            .setInstructions(true) //optional parameter
//            .withListener(new BaatoRouting.BaatoRoutingRequestListener() {
//              @Override
//              public void onSuccess(DirectionsAPIResponse directionResponse) {
//                NavResponse navResponse = directionResponse.getData().get(0);
//                double distanceInKm = navResponse.getDistanceInMeters() / 1000;
//                long time = navResponse.getTimeInMs() / 1000;
//
//                ObjectNode parsedNavigationResponse = NavigateResponseConverter.convertFromGHResponse(directionResponse.getData().get(0), navigationMode);
//                DirectionsResponse directionsResponse = DirectionsResponse.fromJson(String.valueOf(parsedNavigationResponse));
//                route = directionsResponse.routes().get(0);
//                handleRoute(directionsResponse, isOffRoute);
//              }
//
//              @Override
//              public void onFailed(Throwable t) {
//                if (t.getMessage() != null && t.getMessage().contains("Failed to connect"))
//                  Toast.makeText(getApplicationContext(), "Please connect to internet to get the routes!", Toast.LENGTH_SHORT).show();
//
//              }
//            })
//            .doRequest();
  }

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
