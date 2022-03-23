package com.mapbox.services.android.navigation.v5.navigation;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.v5.location.LocationValidator;
import com.mapbox.services.android.navigation.v5.navigation.notification.NavigationNotification;
import com.mapbox.services.android.navigation.v5.route.FasterRoute;
import com.mapbox.services.android.navigation.v5.route.RouteFetcher;

import timber.log.Timber;

/**
 * Internal usage only, use navigation by initializing a new instance of {@link MapboxNavigation}
 * and customizing the navigation experience through that class.
 * <p>
 * This class is first created and started when {@link MapboxNavigation#startNavigation(DirectionsRoute)}
 * get's called and runs in the background until either the navigation sessions ends implicitly or
 * the hosting activity gets destroyed. Location updates are also tracked and handled inside this
 * service. Thread creation gets created in this service and maintains the thread until the service
 * gets destroyed.
 * </p>
 */
public class NavigationService extends Service {

  private final IBinder localBinder = new LocalBinder();
  private RouteProcessorBackgroundThread thread;
  private NavigationLocationEngineUpdater locationEngineUpdater;
  private RouteFetcher routeFetcher;
  private NavigationNotificationProvider notificationProvider;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return localBinder;
  }

  /**
   * Only should be called once since we want the service to continue running until the navigation
   * session ends.
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("NavigationService", "Connected to service.7");
    NavigationTelemetry.getInstance().initializeLifecycleMonitor(getApplication());
    Log.d("NavigationService", "Connected to service.8");
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    stopForeground(true);
    super.onDestroy();
  }

  /**
   * This gets called when {@link MapboxNavigation#startNavigation(DirectionsRoute)} is called and
   * setups variables among other things on the Navigation Service side.
   */
  void startNavigation(MapboxNavigation mapboxNavigation) {
    Log.d("NavigationService", "Connected to service.3");
    initialize(mapboxNavigation);
    Log.d("NavigationService", "Connected to service.4");
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      public void run() {
        startForegroundNotification(notificationProvider.retrieveNotification());
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    Log.d("NavigationService", "Connected to service.5");
    locationEngineUpdater.forceLocationUpdate(mapboxNavigation.getRoute());
    Log.d("NavigationService", "Connected to service.6");
  }

  /**
   * Removes the location / route listeners and  quits the thread.
   */
  void endNavigation() {
    routeFetcher.clearListeners();
    locationEngineUpdater.removeLocationEngineListener();
    notificationProvider.shutdown(getApplication());
    thread.quit();
  }

  /**
   * Called with {@link MapboxNavigation#setLocationEngine(LocationEngine)}.
   * Updates this service with the new {@link LocationEngine}.
   *
   * @param locationEngine to update the provider
   */
  void updateLocationEngine(LocationEngine locationEngine) {
    locationEngineUpdater.updateLocationEngine(locationEngine);
  }

  private void initialize(final MapboxNavigation mapboxNavigation) {
    NavigationEventDispatcher dispatcher = mapboxNavigation.getEventDispatcher();
    String accessToken = mapboxNavigation.obtainAccessToken();
    Log.d("NavigationService", "Connected to service.9");
    initializeRouteFetcher(dispatcher, accessToken, mapboxNavigation.retrieveEngineProvider());
    Log.d("NavigationService", "Connected to service.10");
//    new Handler(Looper.getMainLooper()).post(new Runnable() {
//      public void run() {
        initializeNotificationProvider(mapboxNavigation);
//        try {
//          Thread.sleep(10000);
//        } catch (InterruptedException e) {
//          Log.d("NavigationService", "initializeNotificationProvider exception");
//          e.printStackTrace();
//        }
//      }
//    });
    Log.d("NavigationService", "Connected to service.11");
    initializeRouteProcessorThread(dispatcher, routeFetcher, notificationProvider);
    Log.d("NavigationService", "Connected to service.12");
    initializeLocationProvider(mapboxNavigation);
  }

  private void initializeRouteFetcher(NavigationEventDispatcher dispatcher, String accessToken,
                                      NavigationEngineFactory engineProvider) {
    FasterRoute fasterRouteEngine = engineProvider.retrieveFasterRouteEngine();
    NavigationFasterRouteListener listener = new NavigationFasterRouteListener(dispatcher, fasterRouteEngine);
    routeFetcher = new RouteFetcher(getApplication(), accessToken);
    routeFetcher.addRouteListener(listener);
  }

  private void initializeNotificationProvider(MapboxNavigation mapboxNavigation) {
    Log.d("NavigationService", "initializeNotificationProvider: 1");
    notificationProvider = new NavigationNotificationProvider(getApplication(), mapboxNavigation);
    Log.d("NavigationService", "initializeNotificationProvider: 2");
  }

  private void initializeRouteProcessorThread(NavigationEventDispatcher dispatcher, RouteFetcher routeFetcher,
                                              NavigationNotificationProvider notificationProvider) {
    RouteProcessorThreadListener listener = new RouteProcessorThreadListener(
      dispatcher, routeFetcher, notificationProvider
    );
    thread = new RouteProcessorBackgroundThread(new Handler(), listener);
  }

  private void initializeLocationProvider(MapboxNavigation mapboxNavigation) {
    LocationEngine locationEngine = mapboxNavigation.getLocationEngine();
    int accuracyThreshold = mapboxNavigation.options().locationAcceptableAccuracyInMetersThreshold();
    LocationValidator validator = new LocationValidator(accuracyThreshold);
    NavigationLocationEngineListener listener = new NavigationLocationEngineListener(
      thread, mapboxNavigation, locationEngine, validator
    );
    locationEngineUpdater = new NavigationLocationEngineUpdater(locationEngine, listener);
  }

  private void startForegroundNotification(NavigationNotification navigationNotification) {
    final Notification notification = navigationNotification.getNotification();
    final int notificationId = navigationNotification.getNotificationId();
    notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
//    new Handler(Looper.getMainLooper()).post(new Runnable() {
//      public void run() {
        startForeground(notificationId, notification);
//        try {
//          Thread.sleep(10000);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//    });
//    startForeground(notificationId, notification);
  }

  class LocalBinder extends Binder {
    NavigationService getService() {
      Timber.d("Local binder called.");
      return NavigationService.this;
    }
  }
}
