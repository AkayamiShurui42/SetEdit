import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
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
import io.github.muntashirakon.setedit.utils.AndroidPropertyUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;
import io.github.muntashirakon.setedit.utils.SettingsUtils;
import rikka.shizuku.Shizuku;

public class SettingsMonitorService extends Service {
    public static final String TAG = "SettingsMonitor";
    public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".notification.MONITOR_SERVICE";

    private Handler handler;
    private SettingsObserver systemObserver;
    private SettingsObserver secureObserver;
    private SettingsObserver globalObserver;
    private Shizuku.OnBinderReceivedListener shizukuListener;
    private final Runnable periodicCheck = new Runnable() {
        @Override
        public void run() {
            applyAllLockedSettings();
            if (handler != null) {
                handler.postDelayed(this, 30000); // Check every 30 seconds
            }
        }
    };

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(2, notification);
        }

        handler = new Handler(Looper.getMainLooper());

        systemObserver = new SettingsObserver(handler, SettingsType.SYSTEM_SETTINGS, TableType.TABLE_SYSTEM);
        secureObserver = new SettingsObserver(handler, SettingsType.SECURE_SETTINGS, TableType.TABLE_SECURE);
        globalObserver = new SettingsObserver(handler, SettingsType.GLOBAL_SETTINGS, TableType.TABLE_GLOBAL);

        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, systemObserver);
        getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, secureObserver);
        getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, globalObserver);

        applyAllLockedSettings();
        handler.postDelayed(periodicCheck, 30000);

        shizukuListener = new Shizuku.OnBinderReceivedListener() {
            @Override
            public void onBinderReceived() {
                applyAllLockedSettings();
            }
        };
        try {
            Shizuku.addBinderReceivedListenerSticky(shizukuListener);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to register Shizuku listener", t);
        }
    }

    private void applyAllLockedSettings() {
        SharedPreferences lockedPrefs = getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = lockedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String fullKey = entry.getKey();
            if (fullKey == null) continue;

            int lastColon = fullKey.lastIndexOf(':');
            if (lastColon <= 0 || lastColon == fullKey.length() - 1) continue;

            String key = fullKey.substring(0, lastColon);
            String tableType = fullKey.substring(lastColon + 1);
            String settingsType = null;
            String finalSavedValue = String.valueOf(entry.getValue());

            if (TableType.TABLE_SYSTEM.equals(tableType)) {
                settingsType = SettingsType.SYSTEM_SETTINGS;
            } else if (TableType.TABLE_SECURE.equals(tableType)) {
                settingsType = SettingsType.SECURE_SETTINGS;
            } else if (TableType.TABLE_GLOBAL.equals(tableType)) {
                settingsType = SettingsType.GLOBAL_SETTINGS;
            } else if (TableType.TABLE_PROPERTIES.equals(tableType)) {
                checkAndRevertProperty(key, finalSavedValue);
            }

            if (settingsType != null) {
                checkAndRevertSetting(key, settingsType, tableType);
            }
        }
    }

    private void checkAndRevertProperty(String key, String savedValue) {
        new Thread(() -> {
            Shell.Result result = Shell.cmd("getprop " + key).exec();
            String currentValue = TextUtils.join("", result.getOut()).trim();
            if (!savedValue.equals(currentValue)) {
                Log.i(TAG, "Locked property changed: " + key + ". Reverting to " + savedValue + " (Current: " + currentValue + ")");
                ActionResult updateResult = AndroidPropertyUtils.update(key, savedValue);
                if (!updateResult.successful) {
                    Log.e(TAG, "Failed to revert locked property " + key + ": " + updateResult.getLogs());
                } else {
                    Log.i(TAG, "Successfully reverted locked property: " + key);
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(periodicCheck);
        }
        if (systemObserver != null) getContentResolver().unregisterContentObserver(systemObserver);
        if (secureObserver != null) getContentResolver().unregisterContentObserver(secureObserver);
        if (globalObserver != null) getContentResolver().unregisterContentObserver(globalObserver);
        if (shizukuListener != null) {
            try {
                Shizuku.removeBinderReceivedListener(shizukuListener);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to unregister Shizuku listener", t);
            }
        }
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
        String savedValue = null;
        try {
            savedValue = lockedPrefs.getString(key + ":" + tableType, null);
        } catch (ClassCastException e) {
            Log.e(TAG, "Failed to read locked setting value", e);
        }

        if (savedValue != null) {
            final String finalSavedValue = savedValue;
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
            if (currentValue == null || !currentValue.equals(finalSavedValue)) {
                Log.i(TAG, "Locked setting changed: " + key + ". Reverting to " + finalSavedValue);
                handler.postDelayed(() -> {
                    new Thread(() -> {
                        ActionResult result = SettingsUtils.update(SettingsMonitorService.this, settingsType, key, finalSavedValue);
                        if (!result.successful) {
                            Log.e(TAG, "Failed to revert locked setting " + key + ": " + result.getLogs());
                        } else {
                            Log.i(TAG, "Successfully reverted locked setting: " + key);
                        }
                    }).start();
                }, 500); // 0.5s delay to make sure system finishes its update first
            }
        }
    }
}
