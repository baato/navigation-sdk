//package com.bhawak.osmnavigation;
//
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.bhawak.osmnavigation.navigation.DistanceConfig;
//import com.bhawak.osmnavigation.navigation.DistanceUtils;
//
//import com.bhawak.osmnavigation.navigation.NavResponse;
//import com.bhawak.osmnavigation.navigation.NavigateResponseConverter;
//import com.bhawak.osmnavigation.navigation.NavigateResponseConverterTranslationMap;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.graphhopper.GHResponse;
//import com.graphhopper.util.TranslationMap;
//import com.mapbox.android.core.permissions.PermissionsListener;
//import com.mapbox.android.core.permissions.PermissionsManager;
//import com.mapbox.api.directions.v5.models.DirectionsResponse;
//import com.mapbox.api.directions.v5.models.DirectionsRoute;
//import com.mapbox.geojson.Feature;
//import com.mapbox.geojson.FeatureCollection;
//import com.mapbox.geojson.LineString;
//import com.mapbox.geojson.Point;
//import com.mapbox.mapboxsdk.Mapbox;
//import com.mapbox.mapboxsdk.camera.CameraPosition;
//import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
//import com.mapbox.mapboxsdk.constants.Style;
//import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.geometry.LatLngBounds;
//import com.mapbox.mapboxsdk.location.LocationComponent;
//import com.mapbox.mapboxsdk.location.modes.CameraMode;
//import com.mapbox.mapboxsdk.maps.MapView;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
//import com.mapbox.mapboxsdk.style.layers.LineLayer;
//import com.mapbox.mapboxsdk.style.layers.Property;
//import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
//import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
//import com.mapbox.mapboxsdk.utils.ColorUtils;
//import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
//import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
//import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
//import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.StringWriter;
//import java.io.UnsupportedEncodingException;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
//
//public class MainActivityBefore extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
//    private MapView mapView;
//    private MapboxMap mapboxMap;
//    List<Point> points;
//
//    // variables for adding location layer
//    private PermissionsManager permissionsManager;
//    private LocationComponent locationComponent;
//    // variables for calculating and drawing a route
//
//    private DirectionsRoute currentRoute, directionsRoute;
//    private static final String TAG = "DirectionsActivity";
//    private NavigationMapRoute navigationMapRoute;
//    // variables needed to initialize navigation
//    private Button button;
//    private NavigateResponseConverter navigateResponseConverter;
//    private TranslationMap translationMap;
//    private String encodedPolyline;
//    private static final TranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap().doImport();
//
//    public NavigateResponseConverter getNavigateResponseConverter() {
//        return navigateResponseConverter;
//    }
//
//    public ApiInterface getApiInterface() {
//        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//        httpClient.connectTimeout(100, TimeUnit.SECONDS);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.1.72:8080")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(httpClient.build())
//                .build();
//        return retrofit.create(ApiInterface.class);
//    }
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Mapbox Access token
//        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
////        Mapbox.getInstance(getApplicationContext(), null);
//        setContentView(R.layout.activity_main);
//        mapView = (MapView) findViewById(R.id.mapView);
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);
//
//    }
//
//    @SuppressWarnings({"MissingPermission"})
//    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
//        // Check if permissions are enabled and if not request
//        if (PermissionsManager.areLocationPermissionsGranted(this)) {
//            // Activate the MapboxMap LocationComponent to show user location
//            // Adding in LocationComponentOptions is also an optional parameter
//            locationComponent = mapboxMap.getLocationComponent();
//            locationComponent.activateLocationComponent(this, loadedMapStyle);
//            locationComponent.setLocationComponentEnabled(true);
//            // Set the component's camera mode
//            locationComponent.setCameraMode(CameraMode.TRACKING);
//        } else {
//            permissionsManager = new PermissionsManager(this);
//            permissionsManager.requestLocationPermissions(this);
//        }
//    }
//    private void _launchNavigationWithRoute() {
//        NavigationLauncherOptions.Builder optionsBuilder = NavigationLauncherOptions.builder()
//                .shouldSimulateRoute(false)
//                .waynameChipEnabled(false);
//        optionsBuilder.directionsRoute(directionsRoute);
//        NavigationLauncher.startNavigation(this, optionsBuilder.build());
//    }
//    private void animateCameraBbox(LatLngBounds bounds) {
//        CameraPosition position = mapboxMap.getCameraForLatLngBounds(bounds, new int[]{50,50,50,50});
//        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
//    }
//
//    private void animateCamera(LatLng point) {
//        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 9));
//    }
//    private void boundCameraToRoute() {
//        if (directionsRoute != null) {
//
//            List<List<Double>> points = DecodeLine.decodePolyline(directionsRoute.geometry(),false);
//            List<LatLng> bboxPoints = new ArrayList<>();
//            for (List<Double> point : points) {
//                bboxPoints.add(new LatLng(point.get(0), point.get(1)));
//            }
//            if (bboxPoints.size() > 1) {
//                try {
//                    LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
//                    // left, top, right, bottom
//                    animateCameraBbox(bounds);
//                } catch (InvalidLatLngBoundsException exception) {
//                    Toast.makeText(this, "Valid Route Not Found", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    @Override
//    public void onExplanationNeeded(List<String> permissionsToExplain) {
//        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
//    }
//
//
//    @Override
//    public void onPermissionResult(boolean granted) {
//        if (granted) {
//            enableLocationComponent(mapboxMap.getStyle());
//        } else {
//            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
//            finish();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        this.mapView.onStart();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mapView.onStop();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override
//    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
//        this.mapboxMap = mapboxMap;
////       new Style.Builder().fromUri("https://api.myjson.com/bins/xeeee");
//        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
//            @Override
//            public void onStyleLoaded(@NonNull Style style) {
//                mapboxMap.setCameraPosition(new CameraPosition.Builder()
//                        .target(new LatLng(27.7172, 85.3240))
//                        .zoom(12)
//                        .build());
//                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
////                new DrawGeoJson(MainActivity.this).execute();
//                enableLocationComponent(style);
//                addDestinationIconSymbolLayer(style);
//                addPolylinelayer(style);
//                mapboxMap.addOnMapClickListener(MainActivityBefore.this);
//                button = findViewById(R.id.startButton);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        boolean simulateRoute = false;
//                        try {
//                            DirectionsResponse directionsResponse = DirectionsResponse.fromJson(returnFromRaw());
//
//                            DirectionsRoute locle = directionsResponse.routes().get(0);
//                            String code = "Ok";
//                            String message = "";
//                            Log.wtf("Local Direction", String.valueOf(locle));
//                            navMapRoute(locle);
//                            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                    .directionsRoute(locle)
//                                    .shouldSimulateRoute(simulateRoute)
//                                    .build();
//                            NavigationLauncher.startNavigation(MainActivityBefore.this, options);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        _launchNavigationWithRoute();
////                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
////                                .directionsRoute(currentRoute)
////                                .shouldSimulateRoute(simulateRoute)
////                                .build();
////                        NavigationLauncher.startNavigation(MainActivity.this, options);
//
//                    }
//                });
//
//
//            }
//        });
//    }
//    private void navMapRoute(DirectionsRoute myRoute){
//        if (navigationMapRoute != null) {
//                            navigationMapRoute.removeRoute();
//        } else {
//            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
//        }
//        navigationMapRoute.addRoute(myRoute);
//    }
//    private String returnFromRaw() throws IOException {
//        InputStream is = getResources().openRawResource(R.raw.mapboxtest);
//        Writer writer = new StringWriter();
//        char[] buffer = new char[1024];
//        try {
//            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            int n;
//            while ((n = reader.read(buffer)) != -1) {
//                writer.write(buffer, 0, n);
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            is.close();
//        }
//
//        String jsonString = writer.toString();
//        Log.wtf("jsonString", jsonString);
//        return jsonString;
//    }
//
//    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
//        loadedMapStyle.addImage("destination-icon-id",
//                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
//        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
//        loadedMapStyle.addSource(geoJsonSource);
//        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
//        destinationSymbolLayer.withProperties(
//                iconImage("destination-icon-id"),
//                iconAllowOverlap(true),
//                iconIgnorePlacement(true)
//        );
//        loadedMapStyle.addLayer(destinationSymbolLayer);
//    }
//
//    private void addPolylinelayer(@NonNull Style loadedMapStyle) {
//        GeoJsonSource geoJsonSource = new GeoJsonSource("route");
//        loadedMapStyle.addSource(geoJsonSource);
//        LineLayer lineLayer = new LineLayer("line-layer", "route");
//        lineLayer.withProperties(lineColor(ColorUtils.colorToRgbaString(Color.parseColor("#f20c0c"))),
//                visibility(Property.VISIBLE),
//                lineWidth(3f));
//        loadedMapStyle.addLayer(lineLayer);
//
//    }
//
//    @SuppressWarnings({"MissingPermission"})
//    @Override
//    public boolean onMapClick(@NonNull LatLng point) {
//
//        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
//        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
//                locationComponent.getLastKnownLocation().getLatitude());
//
//        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
//        if (source != null) {
//            source.setGeoJson(Feature.fromGeometry(destinationPoint));
//        }
//
//        getRoute(originPoint, destinationPoint);
////        getGHRoute(originPoint, destinationPoint);
//        button.setEnabled(true);
//        button.setBackgroundResource(R.color.mapbox_blue);
//        return true;
//    }
//
//    private void getRoute(Point origin, Point destination) {
//        String[] points = new String[2];
//        points[0] = origin.latitude() + "," + origin.longitude();
////        points[0] = "27.713042695157757,85.2703857421875";
//        points[1] = destination.latitude() + "," + destination.longitude();
//        Call<NavResponse> call = getApiInterface().getRoutes(points, "car", false);
//        call.enqueue(new Callback<NavResponse>() {
//            @Override
//            public void onResponse(Call<NavResponse> call, Response<NavResponse> response) {
//                if (response.body() == null) {
//                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
//                    return;
//                } else if (response.body().getInstructionList() != null && response.body().getInstructionList().size() == 0 && !response.body().getPath().hasErrors()) {
//                    Log.e(TAG, "No routes found");
//                    return;
//                }
//                encodedPolyline = response.body().getEncoded_polyline();
//                initRouteCoordinates();
//
//                ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(response.body(), Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, translationMap, navigateResponseConverterTranslationMap, Locale.ENGLISH));
////                ObjectMapper mapper = new ObjectMapper();
////                mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
////                ObjectNode node = mapper.getNodeFactory().objectNode();
////                encodedPolyline = response.body().getEncoded_polyline();
////                currentRoute = response.body().getEncoded_polyline();
////                else if (response.body().getAll().size() < 1) {
////                    Log.e(TAG, "No routes found");
////                    return;
////                }
//                Log.d(TAG, "Request:" + call.request());
//                Log.d(TAG, "onResponse: " + response.body().toString());
////                Timber.d(response.body().toString());
//                DirectionsResponse directionsResponse = DirectionsResponse.fromJson(obj.toString());
//                directionsRoute = directionsResponse.routes().get(0);
//                navMapRoute(directionsRoute);
//                boundCameraToRoute();
////                Log.wtf("My route",String.valueOf(directionsRoute));
//
////                Timber.d(String.valueOf(obj));
////                addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(PolylineUtils.simplify(points, 0.001))), "#3bb2d0");
//            }
//
//            @Override
//            public void onFailure(Call<NavResponse> call, Throwable t) {
//                Log.d(TAG, "onFailure: " + t);
//                Log.d(TAG, "Request:" + call.request());
//            }
//        });
//        NavigationRoute.builder(this)
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
//                        currentRoute = response.body().routes().get(0);
////                        Log.wtf("Mapbox route 0", String.valueOf(currentRoute));
//                        navMapRoute(currentRoute);
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                        Log.e(TAG, "Error: " + throwable.getMessage());
//                    }
//                });
//    }
//    private void getGHRoute(Point origin, Point destination) {
//        String[] points = new String[2];
//        points[0] = origin.latitude() + "," + origin.longitude();
////        points[0] = "27.713042695157757,85.2703857421875";
//        points[1] = destination.latitude() + "," + destination.longitude();
//        Call<GHResponse> call = getApiInterface().getGHRoutes(points, "car", false);
//        call.enqueue(new Callback<GHResponse>() {
//            @Override
//            public void onResponse(Call<GHResponse> call, Response<GHResponse> response) {
//                if (response.body() == null) {
//                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
//                    return;
//                }
////                null, Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, translationMap, navigateResponseConverterTranslationMap, Locale.ENGLISH));
////                        }
////                        navigationMapRoute.addRoute(currentRoute);
//
//
//                //                else if (response.body().getAll().size() < 1) {
////                    Log.e(TAG, "No routes found");
////                    return;
////                }
//                Log.d(TAG, "Request:" + call.request());
////                Timber.d("onResponse: " + response.body().getBest());
////                ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(response.body(), Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, translationMap, navigateResponseConverterTranslationMap, Locale.ENGLISH));
//
////                addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(PolylineUtils.simplify(points, 0.001))), "#3bb2d0");
//            }
//
//            @Override
//            public void onFailure(Call<GHResponse> call, Throwable t) {
//                Log.d(TAG, "onFailure: " + t);
//                Log.d(TAG, "Request:" + call.request());
//            }
//        });
//    }
//    private void initRouteCoordinates() {
//        points = new ArrayList<>();
//        for (List<Double> coordinates :
//                DecodeLine.decodePolyline(encodedPolyline, false)) {
//            points.add(Point.fromLngLat(coordinates.get(1), coordinates.get(0)));
//        }
//        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("route");
//        if (source != null) {
//            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]
//                    {Feature.fromGeometry(LineString.fromLngLats(points
//                    ))}));
//        }
//    }
//}
//
