package com.bhawak.osmnavigation;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bhawak.osmnavigation.navigation.DistanceConfig;
import com.bhawak.osmnavigation.navigation.DistanceUtils;

import com.bhawak.osmnavigation.navigation.NavResponse;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverter;
import com.bhawak.osmnavigation.navigation.NavigateResponseConverterTranslationMap;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;

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

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener, LocationEngineListener, OnRouteSelectionChangeListener, PermissionsListener, OnNavigationReadyCallback {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final int CHANGE_SETTING_REQUEST_CODE = 1;

    private LocationLayerPlugin locationLayer;
    private LocationEngine locationEngine;
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
    private TranslationMap translationMap;
    private static final TranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap().doImport();
    private boolean isInTrackingMode;


    private boolean locationFound;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    public NavigateResponseConverter getNavigateResponseConverter() {
        return navigateResponseConverter;
    }

    public static ApiInterface getApiInterface() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(100, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://178.128.59.143:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(ApiInterface.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for location permission
        permissionsManager = new PermissionsManager(this);
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this);
        }
        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), "pk.eyJ1IjoiYmhhd2FrIiwiYSI6ImNqdXJ1d3dkNzBmODIzeW42OGxsYzM2ZmMifQ.pw4f4jlgom6wSzovGQIT7w");
//        "pk.xxx"
//        getString(R.string.mapbox_access_token);
//        Mapbox.getInstance(getApplicationContext(),null);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        button = findViewById(R.id.startButton);
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
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(BALANCED_POWER_ACCURACY);
        locationEngine.setInterval(0);
        locationEngine.setFastestInterval(1000);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();

        if (locationEngine.getLastLocation() != null) {
            Location lastLocation = locationEngine.getLastLocation();
            onLocationChanged(lastLocation);
            currentLocation = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
        }
    }
    @Override
    public void onPermissionResult(boolean granted) {
        if (!granted) {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
        }
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
        locationLayer = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationLayer.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        initLocationEngine();
        initLocationLayer();

        mapboxMap.setStyleUrl("http://178.128.59.143:8080/api/v2/styles/a1e37ae99cdb4f29910cdf27a51a0282.json", new MapboxMap.OnStyleLoadedListener() {
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

                        Log.d("current route:", String.valueOf(currentRoute));
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        NavigationLauncher.startNavigation(MainActivity.this, options);
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
        InputStream is = getResources().openRawResource(R.raw.mapboxtest);
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

    @Override
    public void onConnected() {
        findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInTrackingMode) {
                    isInTrackingMode = true;
                    locationLayer.setCameraMode(CameraMode.TRACKING);
                    locationLayer.zoomWhileTracking(16f);
                    Toast.makeText(MainActivity.this, "tracking enabled",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "tracking already enabled",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

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
        Point originPoint = Point.fromLngLat(85.3407169, 27.7244709);
        getRoute(originPoint, destinationPoint);
//        if (locationEngine.getLastLocation() != null) {
//          Point originPoint = Point.fromLngLat(locationEngine.getLastLocation().getLongitude(),
//                    locationEngine.getLastLocation().getLatitude());
//            getRoute(originPoint, destinationPoint);
//        }
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
        Call<NavAPIResponse> call = getApiInterface().getNavigationRoute("token.1NsVYoB2lSogxPMlthmHPM9jxtNccGbnkajFd7x5dgjI", points, "car", false, true);
        call.enqueue(new Callback<NavAPIResponse>() {
            @Override
            public void onResponse(Call<NavAPIResponse> call, Response<NavAPIResponse> response) {
//                Log.d("request::", String.valueOf(call.request()));
//                Log.d(TAG, "onResponse: " + String.valueOf(call.request()));
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().data.get(0).getInstructionList() != null && response.body().data.get(0).getInstructionList().size() == 0) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                NavResponse navResponse = response.body().data.get(0);
                encodedPolyline = navResponse.getEncoded_polyline();
//                Timber.wtf("Anno: " + String.valueOf(response.body().getPath().getInstructions().get(0).getAnnotation()));
//                encodedPolyline = response.body().getEncoded_polyline();
                initRouteCoordinates();

                ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(navResponse, Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, translationMap, navigateResponseConverterTranslationMap, Locale.ENGLISH));
//                Timber.d( "MapObj" + obj);
//                Log.d(TAG, "onResponse: " + response.body().toString());
//                Timber.d(response.body().toString());
                DirectionsResponse directionsResponse = DirectionsResponse.fromJson(obj.toString());

                currentRoute = directionsResponse.routes().get(0);
//                try {
//                    currentRoute = DirectionsResponse.fromJson(returnFromRaw()).routes().get(0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                Timber.d("On direction:" + obj);
                navMapRoute(currentRoute);
//                boundCameraToRoute();
//                Log.wtf("My route",String.valueOf(directionsRoute));

//                Timber.d(String.valueOf(obj));
//                addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(PolylineUtils.simplify(points, 0.001))), "#3bb2d0");
            }

            @Override
            public void onFailure(Call<NavAPIResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Log.d(TAG, "Request:" + call.request());
            }
        });
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
//                        directionsRoute = response.body().routes().get(0);
//                        Timber.tag("Mapbox route 0").wtf(directionsRoute.routeOptions().toJson());
//                        navMapRoute(directionsRoute);
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                        Log.e(TAG, "Error: " + throwable.getMessage());
//                    }
//                });
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        mapboxMap.setStyleUrl("http://178.128.59.143:8080/api/v2/styles/a1e37ae99cdb4f29910cdf27a51a0282.json", new MapboxMap.OnStyleLoadedListener() {
            @Override
            public void onStyleLoaded(@NonNull String style) {
            }
        });
    }
}

