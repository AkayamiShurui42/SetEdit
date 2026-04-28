package io.github.muntashirakon.setedit.boot;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

import io.github.muntashirakon.setedit.BuildConfig;
import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.SettingsType;
import io.github.muntashirakon.setedit.TableType;
import io.github.muntashirakon.setedit.utils.SettingsUtils;

public class SettingsMonitorService extends Service {
    public static final String TAG = "SettingsMonitor";
    public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".notification.MONITOR_SERVICE";

    private Handler handler;
    private SettingsObserver systemObserver;
    private SettingsObserver secureObserver;
    private SettingsObserver globalObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Settings Monitor Active")
                .setContentText("Monitoring locked settings...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
        startForeground(2, notification);

        handler = new Handler(Looper.getMainLooper());

        systemObserver = new SettingsObserver(handler, SettingsType.SYSTEM_SETTINGS, TableType.TABLE_SYSTEM);
        secureObserver = new SettingsObserver(handler, SettingsType.SECURE_SETTINGS, TableType.TABLE_SECURE);
        globalObserver = new SettingsObserver(handler, SettingsType.GLOBAL_SETTINGS, TableType.TABLE_GLOBAL);

        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, systemObserver);
        getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, secureObserver);
        getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, globalObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (systemObserver != null) getContentResolver().unregisterContentObserver(systemObserver);
        if (secureObserver != null) getContentResolver().unregisterContentObserver(secureObserver);
        if (globalObserver != null) getContentResolver().unregisterContentObserver(globalObserver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationChannelCompat notificationChannel = new NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
                .setVibrationEnabled(false)
                .setName("Settings Monitor")
                .build();
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private class SettingsObserver extends ContentObserver {
        private final String settingsType;
        private final String tableType;

        public SettingsObserver(Handler handler, String settingsType, String tableType) {
            super(handler);
            this.settingsType = settingsType;
            this.tableType = tableType;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri != null) {
                String key = uri.getLastPathSegment();
                if (key != null) {
                    checkAndRevertSetting(key, settingsType, tableType);
                }
            }
        }
    }

    private void checkAndRevertSetting(String key, String settingsType, String tableType) {
        SharedPreferences lockedPrefs = getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
        String savedValue = lockedPrefs.getString(key + ":" + tableType, null);
        if (savedValue != null) {
            String currentValue = null;
            try {
                if (SettingsType.SYSTEM_SETTINGS.equals(settingsType)) {
                    currentValue = Settings.System.getString(getContentResolver(), key);
                } else if (SettingsType.SECURE_SETTINGS.equals(settingsType)) {
                    currentValue = Settings.Secure.getString(getContentResolver(), key);
                } else if (SettingsType.GLOBAL_SETTINGS.equals(settingsType)) {
                    currentValue = Settings.Global.getString(getContentResolver(), key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Only revert if changed
            if (currentValue == null || !currentValue.equals(savedValue)) {
                Log.i(TAG, "Locked setting changed: " + key + ". Reverting to " + savedValue);
                handler.postDelayed(() -> {
                    SettingsUtils.update(SettingsMonitorService.this, settingsType, key, savedValue);
                }, 500); // 0.5s delay to make sure system finishes its update first
            }
        }
    }
}
