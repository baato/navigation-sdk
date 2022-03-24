package com.mapbox.services.android.navigation.v5.navigation.telemetry.errors;

import static com.mapbox.services.android.navigation.v5.navigation.telemetry.MapboxTelemetryConstants.MAPBOX_TELEMETRY_PACKAGE;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mapbox.services.android.navigation.v5.navigation.telemetry.CrashEvent;
import com.mapbox.services.android.navigation.v5.navigation.telemetry.FileUtils;
import com.mapbox.services.android.navigation.v5.navigation.telemetry.MapboxTelemetryConstants;

import java.io.File;
import java.util.concurrent.ExecutorService;

public final class ErrorReporterEngine {
  private static final String LOG_TAG = "CrashReporter";

  public static void sendErrorReports(@NonNull final Context context,
                                      @NonNull final ExecutorService executorService) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      LocalBroadcastManager.getInstance(context)
        .sendBroadcast(new Intent(MapboxTelemetryConstants.ACTION_TOKEN_CHANGED));
    } else {
      try {
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            ErrorReporterEngine.sendReports(context);
          }
        });
      } catch (Throwable throwable) {
        Log.e(LOG_TAG, throwable.toString());
      }
    }
  }

  static void sendReports(@NonNull Context context) {
    if (context == null || context.getApplicationContext() == null) {
      return;
    }

    File rootDirectory = FileUtils.getFile(context.getApplicationContext(), MAPBOX_TELEMETRY_PACKAGE);
    if (!rootDirectory.exists()) {
      Log.w(LOG_TAG, "Root directory doesn't exist");
      return;
    }

    handleErrorReports(ErrorReporterClient
      .create(context.getApplicationContext())
      .loadFrom(rootDirectory));
  }

  @VisibleForTesting
  static void handleErrorReports(@NonNull ErrorReporterClient client) {
    if (!client.isEnabled()) {
      Log.w(LOG_TAG, "Crash reporter is disabled");
      return;
    }

    while (client.hasNextEvent()) {
      CrashEvent event = client.nextEvent();
      if (client.isDuplicate(event)) {
        Log.d(LOG_TAG, "Skip duplicate crash in this batch: " + event.getHash());
        client.delete(event);
        continue;
      }

      if (client.send(event)) {
        client.delete(event);
      } else {
        Log.w(LOG_TAG, "Failed to deliver crash event");
      }
    }
  }
}
