package io.github.muntashirakon.setedit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.SettingsType;
import rikka.shizuku.Shizuku;

public final class SettingsUtils {
    @NonNull
    public static ActionResult create(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName, @NonNull String newValue) {
        return updateInternal(context, settingsType, keyName, newValue, ActionResult.TYPE_CREATE);
    }

    @NonNull
    public static ActionResult update(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName, @NonNull String newValue) {
        return updateInternal(context, settingsType, keyName, newValue, ActionResult.TYPE_UPDATE);
    }

    @NonNull
    public static ActionResult delete(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName) {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                Shell.Result result = Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings delete " + settingsType + " " + keyName).exec();
                return new ActionResult(ActionResult.TYPE_DELETE, result.isSuccess());
            } catch(Exception e) {
                ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
                r.setLogs(e.getMessage());
                return r;
            }
        }
        if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.Result result = Shell.cmd("settings delete " + settingsType + " " + keyName).exec();
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, result.isSuccess());
            r.setLogs(TextUtils.join("\n", result.getErr()));
            return r;
        }
        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) {
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs("Shizuku/Permission request in progress...");
            return r;
        }
        if (!isGranted) {
            if (context instanceof android.app.Activity) {
                EditorUtils.displayGrantPermissionMessage(context);
            }
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs("Permission WRITE_SECURE_SETTINGS missing.");
            return r;
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put("name", keyName);
            contentResolver.delete(Uri.parse("content://settings/" + settingsType), "name = ?", new String[]{keyName});
            return new ActionResult(ActionResult.TYPE_DELETE, true);
        } catch (SecurityException se) {
            Log.e("SettingsUtils", "Permission denied for: " + keyName, se);
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs("Permission denied for: " + keyName);
            return r;
        } catch (Throwable th) {
            th.printStackTrace();
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs(th.getMessage());
            return r;
        }
    }

    @NonNull
    private static ActionResult updateInternal(@NonNull Context context, @SettingsType String settingsType,
                                               @NonNull String keyName, @NonNull String newValue,
                                               @ActionResult.ActionType int actionType) {
        if ("null".equals(newValue)) {
            // Null just clears value not the key
            newValue = "";
        }
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                Shell.Result result = Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings put " + settingsType + " " + keyName + " \"" + newValue + "\"").exec();
                return new ActionResult(actionType, result.isSuccess());
            } catch(Exception e) {
                ActionResult r = new ActionResult(actionType, false);
                r.setLogs(e.getMessage());
                return r;
            }
        }
        if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.Result result = Shell.cmd("settings put " + settingsType + " " + keyName + " \"" + newValue + "\"").exec();
            ActionResult r = new ActionResult(actionType, result.isSuccess());
            r.setLogs(TextUtils.join("\n", result.getErr()));
            return r;
        }
        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) {
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs("Shizuku/Permission request in progress...");
            return r;
        }
        if (!isGranted) {
            if (context instanceof android.app.Activity) {
                EditorUtils.displayGrantPermissionMessage(context);
            }
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs("Permission WRITE_SECURE_SETTINGS missing.");
            return r;
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put("name", keyName);
            contentValues.put("value", newValue);
            contentResolver.insert(Uri.parse("content://settings/" + settingsType), contentValues);
            return new ActionResult(actionType, true);
        } catch (SecurityException se) {
            Log.e("SettingsUtils", "Permission denied for: " + keyName, se);
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs("Permission denied for: " + keyName);
            return r;
        } catch (Throwable th) {
            th.printStackTrace();
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs(th.getMessage());
            return r;
        }
    }
}
