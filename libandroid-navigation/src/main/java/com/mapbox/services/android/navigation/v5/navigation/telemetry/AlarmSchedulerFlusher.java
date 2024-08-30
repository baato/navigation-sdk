package com.mapbox.services.android.navigation.v5.navigation.telemetry;

import static com.mapbox.services.android.navigation.v5.navigation.telemetry.SchedulerFlusherFactory.SCHEDULER_FLUSHER_INTENT;
import static com.mapbox.services.android.navigation.v5.navigation.telemetry.SchedulerFlusherFactory.flushingPeriod;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.VisibleForTesting;

class AlarmSchedulerFlusher implements SchedulerFlusher {
    private final Context context;
    private final AlarmManager manager;
    private final AlarmReceiver receiver;
    private PendingIntent pendingIntent;

    AlarmSchedulerFlusher(Context context, AlarmManager manager, AlarmReceiver receiver) {
        this.context = context;
        this.manager = manager;
        this.receiver = receiver;
    }

    @Override
    public void register() {
        Intent alarmIntent = receiver.supplyIntent();
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, flags);
        IntentFilter filter = new IntentFilter(SCHEDULER_FLUSHER_INTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(receiver, filter,Context.RECEIVER_EXPORTED);
        }else{
            context.registerReceiver(receiver, filter);

        }
    }

    @Override
    public void schedule(long elapsedRealTime) {
        long firstFlushingInMillis = elapsedRealTime + flushingPeriod;
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstFlushingInMillis,
                flushingPeriod, pendingIntent);
    }

    /* only exposed for testing not dealing directly with alarm logic */
    @VisibleForTesting
    boolean scheduleExact(long interval) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval,
                    pendingIntent);
            return true;
        }

        return false;
    }

    @Override
    public void unregister() {
        if (pendingIntent != null) {
            manager.cancel(pendingIntent);
        }
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException exception) {
            // No op for the cases in which the OS has unexpectedly unregistered the alarm
            // Shouldn't happen but seen crashes in Samsung devices
        }
    }

    PendingIntent obtainPendingIntent() {
        return pendingIntent;
    }
}
