package com.bhawak.osmnavigation.navigation.view.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bhawak.osmnavigation.R;
import com.mapbox.services.android.navigation.v5.navigation.notification.NavigationNotification;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import static com.mapbox.services.android.navigation.v5.navigation.NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL;

public class CustomNavigationNotification implements NavigationNotification {

  private static final int CUSTOM_NOTIFICATION_ID = 91234821;
  private static final String CUSTOM_CHANNEL_ID = "91234821";
  private static final String STOP_NAVIGATION_ACTION = "stop_navigation_action";

  private Notification customNotification;
  private NotificationCompat.Builder customNotificationBuilder;
  private NotificationManager notificationManager;
  private BroadcastReceiver stopNavigationReceiver;
  private int numberOfUpdates;

  public CustomNavigationNotification(Context applicationContext) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      startMyOwnForeground(applicationContext);
    else
      startForeground(customNotification, applicationContext);

  }
  @RequiresApi(api = Build.VERSION_CODES.O)
  private void startMyOwnForeground(Context applicationContext){
//    String NOTIFICATION_CHANNEL_ID = String.valueOf(CUSTOM_NOTIFICATION_ID);
    String channelName = "My Background Service";
    NotificationChannel chan = new NotificationChannel(CUSTOM_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);

    NotificationManager manager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    assert manager != null;
    manager.createNotificationChannel(chan);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(applicationContext, CUSTOM_CHANNEL_ID);
    customNotification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle("Baato Navigation Notification")
            .setContentText("Navigation Ready.")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
    startForeground(customNotification, applicationContext);
  }

  private  void startForeground(Notification notification,Context applicationContext){
    notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

    customNotificationBuilder = new NotificationCompat.Builder(applicationContext, NAVIGATION_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle("Baato Navigation Notification")
            .setContentText("Navigation Ready.")
            .setContentIntent(createPendingStopIntent(applicationContext));

    notification = customNotificationBuilder.build();
  }

  @Override
  public Notification getNotification() {
    return customNotification;
  }

  @Override
  public int getNotificationId() {
    return CUSTOM_NOTIFICATION_ID;
  }

  @Override
  public String getNotificationChannelId() {
    return  CUSTOM_CHANNEL_ID;
  }

  @Override
  public void updateNotification(RouteProgress routeProgress) {
    // Update the builder with a new number of updates
    customNotificationBuilder.setContentText("Number of updates: " + numberOfUpdates++);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      notificationManager.notify(Integer.parseInt(CUSTOM_CHANNEL_ID), customNotificationBuilder.build());
    else
      notificationManager.notify(CUSTOM_NOTIFICATION_ID, customNotificationBuilder.build());
  }

  @Override
  public void onNavigationStopped(Context context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      notificationManager.cancel(Integer.parseInt(CUSTOM_CHANNEL_ID));
    else {
      context.unregisterReceiver(stopNavigationReceiver);
      notificationManager.cancel(CUSTOM_NOTIFICATION_ID);
    }
  }

  public void register(BroadcastReceiver stopNavigationReceiver, Context applicationContext) {
    this.stopNavigationReceiver = stopNavigationReceiver;
    applicationContext.registerReceiver(stopNavigationReceiver, new IntentFilter(STOP_NAVIGATION_ACTION));
  }

  private PendingIntent createPendingStopIntent(Context context) {
    Intent stopNavigationIntent = new Intent(STOP_NAVIGATION_ACTION);
    return PendingIntent.getBroadcast(context, 0, stopNavigationIntent, 0);
  }
}
