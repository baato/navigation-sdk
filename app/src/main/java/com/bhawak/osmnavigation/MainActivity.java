package com.bhawak.osmnavigation;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baato.baatolibrary.models.DirectionsAPIResponse;
import com.baato.baatolibrary.services.BaatoRouting;
import com.bhawak.osmnavigation.navigation.DirectionAPIResponse;
import com.bhawak.osmnavigation.navigation.NavResponse;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverter;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverterTranslationMap;
//import com.bhawak.osmnavigation.navigation.SimpleConverter;
//import com.bhawak.osmnavigation.navigation.SimpleConverter;
import com.bhawak.osmnavigation.navigation.view.ComponentNavigationActivity;
import com.bhawak.osmnavigation.navigation.view.Constants;
import com.bhawak.osmnavigation.navigation.view.MockNavigationActivity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.graphhopper.util.TranslationMap;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.route.OnRouteSelectionChangeListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.mapbox.android.core.location.LocationEnginePriority.BALANCED_POWER_ACCURACY;

import static com.mapbox.android.core.location.LocationEnginePriority.HIGH_ACCURACY;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity implements PermissionsListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,OnMapReadyCallback, MapboxMap.OnMapClickListener {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final int CHANGE_SETTING_REQUEST_CODE = 1;

    private LocationLayerPlugin locationLayer;
//    private LocationEngine locationEngine;
    private MapboxMap mapboxMap;
    private NavigateResponseConverter navigateResponseConverter;
    private MapView mapView;
    private Marker currentMarker;
    private Point currentLocation;
    private Point destination;
    private DirectionsRoute route;
    private LocaleUtils localeUtils;
    private PermissionsManager permissionsManager;
    private DirectionsRoute currentRoute, directionsRoute;
    private String encodedPolyline;
    private Button button;
    List<Point> points;
//    private TranslationMap translationMap;
//    private static final TranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap().doImport();
    private boolean isInTrackingMode;


    private boolean locationFound;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private Point originPoint = null;
    private DirectionsResponse directionsResponse = null;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationClient;
    public NavigateResponseConverter getNavigateResponseConverter() {
        return navigateResponseConverter;
    }
    private Location mylocation;

    public static ApiInterface getApiInterface() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(100, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-staging.baato.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(ApiInterface.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for location permission
//        permissionsManager = new PermissionsManager(this);
//        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
//            permissionsManager.requestLocationPermissions(this);
//        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Mapbox Access token
//        Mapbox.getInstance(this.getBaseContext(), "pk.eyJ1IjoiYmhhd2FrIiwiYSI6ImNqdXJ1d3dkNzBmODIzeW42OGxsYzM2ZmMifQ.pw4f4jlgom6wSzovGQIT7w");
//        "pk.xxx"
//        getString(R.string.mapbox_access_token);
        Mapbox.getInstance(getApplicationContext(),null);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        button = findViewById(R.id.startButton);
        mapView.setStyleUrl("http://api-staging.baato.io/api/v1/styles/breeze?key=" + Constants.token);
//        mapView.setStyleUrl("https://bhawak.github.io/testJson/styleFile.json");
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }
    private void initLocationEngine() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            getMyLocation();
//            locationEngine = new LocationEngineProvider(this).obtainLocationEngineBy(LocationEngine.Type.ANDROID);
//            locationEngine.setPriority(HIGH_ACCURACY);
//            locationEngine.setInterval(0);
//            locationEngine.setFastestInterval(1000);
//            locationEngine.addLocationEngineListener((LocationEngineListener) this);
//            locationEngine.activate();
//
//            if (locationEngine.getLastLocation() != null) {
//                Location lastLocation = locationEngine.getLastLocation();
//                onLocationChanged(lastLocation);
//                currentLocation = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
//            } else getMyLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

    }
    @Override
    public void onPermissionResult(boolean granted) {
        Log.d("granted", String.valueOf(granted));
//        if (!granted) {
//            Toast.makeText(this, "You didn't grant location permissions.",
//                    Toast.LENGTH_LONG).show();
//        } else {
        if (granted) {
            Toast.makeText(this, "Please wait ...",
                    Toast.LENGTH_LONG).show();
            getMyLocation();
        }
//        }//        initLocationLayer();
    }

    private void _launchNavigationWithRoute() {
        NavigationLauncherOptions.Builder optionsBuilder = NavigationLauncherOptions.builder()
                .shouldSimulateRoute(false)
                .waynameChipEnabled(false);
        optionsBuilder.directionsRoute(directionsRoute);
        NavigationLauncher.startNavigation(this, optionsBuilder.build());
    }

    private void animateCameraBbox(LatLngBounds bounds) {
        CameraPosition position = mapboxMap.getCameraForLatLngBounds(bounds, new int[]{50, 50, 50, 50});
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }

    private void animateCamera(LatLng point) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 9));
    }

    private void boundCameraToRoute() {
        if (directionsRoute != null) {

            List<List<Double>> points = DecodeLine.decodePolyline(directionsRoute.geometry(), false);
            List<LatLng> bboxPoints = new ArrayList<>();
            for (List<Double> point : points) {
                bboxPoints.add(new LatLng(point.get(1), point.get(0)));
            }
            if (bboxPoints.size() > 1) {
                try {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
                    // left, top, right, bottom
                    animateCameraBbox(bounds);
                } catch (InvalidLatLngBoundsException exception) {
                    Toast.makeText(this, "Valid Route Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationLayer != null) {
            locationLayer.onStart();
        }
    }

    @SuppressWarnings({"MissingPermission"})
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationLayer != null) {
            locationLayer.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    private void initLocationLayer() {
//        locationLayer = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);

        locationLayer = new LocationLayerPlugin(mapView, mapboxMap);
        locationLayer.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        //remove mapbox attribute
        mapboxMap.getUiSettings().setAttributionEnabled(false);
        mapboxMap.getUiSettings().setLogoEnabled(false);

        initLocationEngine();
        initLocationLayer();
//        mapboxMap.setStyleUrl("https://bhawak.github.io/testJson/styleFile.json", new MapboxMap.OnStyleLoadedListener() {
        mapboxMap.setStyleUrl("http://api-staging.baato.io/api/v1/styles/breeze?key=" + Constants.token, new MapboxMap.OnStyleLoadedListener() {
            @Override
            public void onStyleLoaded(@NonNull String style) {
                mapboxMap.addOnMapClickListener(MainActivity.this);
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(27.7172, 85.3240))
                        .zoom(12)
                        .build());
                addDestinationIconSymbolLayer();
                addPolylinelayer();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean simulateRoute = true;

//                        Log.d("current route:", String.valueOf(currentRoute));
//                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                .directionsRoute(currentRoute)
//                                .shouldSimulateRoute(simulateRoute)
//                                .build();
//                        NavigationLauncher.startNavigation(MainActivity.this, options);
                        Intent intent = new Intent(MainActivity.this, MockNavigationActivity.class);
//                        Intent intent = new Intent(MainActivity.this, ComponentNavigationActivity.class);
                        intent.putExtra("Route",directionsResponse);
                        intent.putExtra("origin", originPoint);
                        intent.putExtra("lastLocation", mylocation);
                        startActivity(intent);
                    }
                });
            }
        });
    }
        private void addDestinationIconSymbolLayer() {
        mapboxMap.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        mapboxMap.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        mapboxMap.addLayer(destinationSymbolLayer);
    }

    private void addPolylinelayer() {
        GeoJsonSource geoJsonSource = new GeoJsonSource("route");
        mapboxMap.addSource(geoJsonSource);
        LineLayer lineLayer = new LineLayer("line-layer", "route");
        lineLayer.withProperties(lineColor(ColorUtils.colorToRgbaString(Color.parseColor("#f20c0c"))),
                visibility(Property.VISIBLE),
                lineWidth(3f));
        mapboxMap.addLayer(lineLayer);

    }
    private void navMapRoute(DirectionsRoute myRoute){
        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
        } else {
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        }
        navigationMapRoute.addRoute(myRoute);
    }
        private String returnFromRaw() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.baatomodified);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        Log.wtf("jsonString", jsonString);
        return jsonString;
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.directions);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

//    @Override
//    public void onConnected() {
//        locationEngine.requestLocationUpdates();
////        locationLayer.setCameraMode(CameraMode.TRACKING_GPS);
////        locationLayer.zoomWhileTracking(16f);
//        findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                    isInTrackingMode = true;
//                    locationLayer.setCameraMode(CameraMode.TRACKING_GPS);
//                    locationLayer.zoomWhileTracking(16f);
//
//            }
//        });
//    }
    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    permissionLocation = checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    if (mylocation != null) {
                        originPoint = Point.fromLngLat(mylocation.getLongitude(),
                                mylocation.getLatitude());
                        locationLayer.forceLocationUpdate(mylocation);
                    }
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
                                    if (mylocation != null) {
                                        originPoint = Point.fromLngLat(mylocation.getLongitude(),
                                                mylocation.getLatitude());
                                        locationLayer.forceLocationUpdate(mylocation);
                                    }
//                                    locationEngine.activate();
                                }

                                if (getIntent().getExtras() != null && getIntent().hasExtra("lat") && getIntent().hasExtra("long")) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            originPoint = Point.fromLngLat(mylocation.getLongitude(),
                                                    mylocation.getLatitude());
//                                            onMapClick(new LatLng(getIntent().getDoubleExtra("lat", 0.00), getIntent().getDoubleExtra("long", 0.00)));
                                        }
                                    }, 2000);
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
    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }



    @Override
    public void onLocationChanged(Location location) {
        mylocation =location;
        originPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        GeoJsonSource source = mapboxMap.getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        button.setEnabled(true);
        button.setBackgroundResource(R.color.mapbox_blue);
//        Point originPoint = Point.fromLngLat(85.3407169, 27.7244709);
//        getRoute(originPoint, destinationPoint);
//        if (locationEngine.getLastLocation() != null) {
//          originPoint = Point.fromLngLat(locationEngine.getLastLocation().getLongitude(),
//                    locationEngine.getLastLocation().getLatitude());
//          getRoute(originPoint, destinationPoint);
//        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            originPoint = Point.fromLngLat(location.getLongitude(),
                    location.getLatitude());
                            getRoute(originPoint, destinationPoint);
                            // Logic to handle location object
                        }
                    }
                });
    }

    private void initRouteCoordinates() {
        points = new ArrayList<>();
        for (List<Double> coordinates :
                DecodeLine.decodePolyline(encodedPolyline, false)) {
            points.add(Point.fromLngLat(coordinates.get(1), coordinates.get(0)));
        }
        GeoJsonSource source = mapboxMap.getSourceAs("route");
        if (source != null) {
            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]
                    {Feature.fromGeometry(LineString.fromLngLats(points
                    ))}));
        }
    }
    private void getRoute(Point origin, Point destination) {
        String[] points = new String[2];
        points[0] = origin.latitude() + "," + origin.longitude();
//        points[0] = "27.713042695157757,85.2703857421875";
        points[1] = destination.latitude() + "," + destination.longitude();
//        new BaatoRouting(this)
//                .setPoints(points)
//                .setAccessToken(Constants.token)
//                .setMode("car") //eg bike, car, foot
//                .setAlternatives(false) //optional parameter
//                .setInstructions(true) //optional parameter
//                .setAPIBaseURL("http://api-staging.baato.io/api/")//optional parameter
//                .setAPIVersion("1")
//                .withListener(new BaatoRouting.BaatoRoutingRequestListener() {
//                    @Override
//                    public void onSuccess(DirectionsAPIResponse directionResponse) {
//                        Log.wtf("Graph:", String.valueOf(directionResponse));
//                       NavResponse navResponse = directionResponse.getData().get(0);
//                        double distanceInKm = navResponse.getDistanceInMeters() / 1000;
//                        long time = navResponse.getTimeInMs() / 1000;
//                        ObjectNode parsedNavigationResponse = NavigateResponseConverter.convertFromGHResponse(directionResponse.getData().get(0), "car");
//                        encodedPolyline = navResponse.getEncoded_polyline();
//                        initRouteCoordinates();
//                        directionsResponse = DirectionsResponse.fromJson(String.valueOf(parsedNavigationResponse));
//                        Timber.wtf("Direction Res:%s", String.valueOf(parsedNavigationResponse));
////                        Log.wtf("ghResponse:",String.valueOf(parsedNavigationResponse));
//                        currentRoute = directionsResponse.routes().get(0);
//                    }
//
//                    @Override
//                    public void onFailed(Throwable t) {
//                        if (t.getMessage() != null && t.getMessage().contains("Failed to connect"))
//                            Toast.makeText(getApplicationContext(), "Please connect to internet to get the routes!", Toast.LENGTH_SHORT).show();
//
//                    }
//                })
//                .doRequest();

        // Mark: MapBox
//                NavigationRoute.builder(this)
//                .accessToken(Mapbox.getAccessToken())
//                .origin(origin)
//                .destination(destination)
//                .build()
//                .getRoute(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//// You can get the generic HTTP info about the response
//                        Log.d(TAG, "Response code: " + response.body().routes());
//                        Log.d(TAG, response.raw().toString());
//                        if (response.body() == null) {
//                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
//                            return;
//                        } else if (response.body().routes().size() < 1) {
//                            Log.e(TAG, "No routes found");
//                            return;
//                        }
////                        directionsRoute = response.body().routes().get(0);
////                        Timber.tag("Mapbox route 0").wtf(directionsRoute.routeOptions().toJson());
////                        navMapRoute(directionsRoute);
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                        Log.e(TAG, "Error: " + throwable.getMessage());
//                    }
//                });

        //Debug version

//        try {
////            DirectionsResponse response = new Gson().fromJson(returnFromRaw(), DirectionsResponse.class);
//            directionsResponse = DirectionsResponse.fromJson(returnFromRaw());
//            encodedPolyline = directionsResponse.routes().get(0).geometry();
//            initRouteCoordinates();
//            currentRoute = directionsResponse.routes().get(0);
//            navMapRoute(currentRoute);
//            /*
//            NavResponse navResponse = response.getData().get(0);
//            encodedPolyline = navResponse.getEncoded_polyline();
//            initRouteCoordinates();
//            ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(navResponse, "car");
//            directionsResponse = DirectionsResponse.fromJson(obj.toString());
//            currentRoute = directionsResponse.routes().get(0);
//            navMapRoute(currentRoute);
//             */
//
//            boundCameraToRoute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        Call<DirectionAPIResponse> call = getApiInterface().getRoutes(Constants.token, points, "car", false, true);
//        call.enqueue(new Callback<DirectionAPIResponse>() {
//            @Override
//            public void onResponse(Call<DirectionAPIResponse> call, Response<DirectionAPIResponse> response) {
//
//                if (response.body() != null && response.body().getMessage().equals("Success")) {
//                    NavResponse navResponse = response.body().getData().get(0);
//                    encodedPolyline = navResponse.getEncoded_polyline();
//                    initRouteCoordinates();
//
//                    ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(navResponse, "car");
//                    directionsResponse = DirectionsResponse.fromJson(obj.toString());
////                currentRoute = directionsResponse.routes().get(0);
//                    currentRoute = directionsResponse.routes().get(0);
//                    navMapRoute(currentRoute);
//                    boundCameraToRoute();
//                Log.wtf("My route",String.valueOf(obj));
//                Log.wtf("request", String.valueOf(call.request()));
//
//                Timber.d(String.valueOf(obj));
////                addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(PolylineUtils.simplify(points, 0.001))), "#3bb2d0");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DirectionAPIResponse> call, Throwable t) {
//                Log.d(TAG, "onFailure: " + t);
//                Log.d(TAG, "Request:" + call.request());
//            }
//        });
        Call<NavAPIResponse> call = getApiInterface().getNavigationRoute(Constants.token, points, "car", false, true);
        call.enqueue(new Callback<NavAPIResponse>() {
            @Override
            public void onResponse(Call<NavAPIResponse> call, Response<NavAPIResponse> response) {

                if (response.body() != null && response.body().getMessage().equals("Success")) {
                    NavigationResponse navResponse = response.body().getData().get(0);
                    encodedPolyline = navResponse.getEncoded_polyline();
                    initRouteCoordinates();
                    Locale locale = new Locale("ne", "NP");
//                    Locale locale = new Locale("en", "US");
                    ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(navResponse, "car", locale);
                    directionsResponse = DirectionsResponse.fromJson(obj.toString());
//                currentRoute = directionsResponse.routes().get(0);
                    currentRoute = directionsResponse.routes().get(0);
                    navMapRoute(currentRoute);
                    boundCameraToRoute();
                    Log.wtf("My route",String.valueOf(obj));
                    Log.wtf("request", String.valueOf(call.request()));

                    Timber.d(String.valueOf(obj));
//                addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(PolylineUtils.simplify(points, 0.001))), "#3bb2d0");
                }
            }

            @Override
            public void onFailure(Call<NavAPIResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Log.d(TAG, "Request:" + call.request());
            }
        });
    }


//    @Override
//    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
//
//    }
//
//    @Override
//    public void onNavigationReady(boolean isRunning) {
//        mapboxMap.setStyleUrl("http://baato.io/api/v1/styles/retro?key=bpk.gzol0CR64kZIci7xHHUujpXdivhz7L2OPRW5rTYMTXHh", new MapboxMap.OnStyleLoadedListener() {
//            @Override
//            public void onStyleLoaded(@NonNull String style) {
//            }
//        });
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

