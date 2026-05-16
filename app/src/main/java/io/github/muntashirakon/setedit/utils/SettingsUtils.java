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
    private static ActionResult runPrivileged(@ActionResult.ActionType int actionType, String cmd, String... args) {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                String[] fullCmd = new String[args.length + 1];
                fullCmd[0] = cmd;
                System.arraycopy(args, 0, fullCmd, 1, args.length);
                rikka.shizuku.ShizukuRemoteProcess process = io.github.muntashirakon.setedit.EditorUtils.newShizukuProcess(fullCmd, null, null);
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return new ActionResult(actionType, true);
                } else {
                    ActionResult r = new ActionResult(actionType, false);
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    r.setLogs(sb.toString().trim());
                    return r;
                }
            } catch (Exception e) {
                ActionResult r = new ActionResult(actionType, false);
                r.setLogs(e.getMessage());
                return r;
            }
        } else if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            StringBuilder sb = new StringBuilder(cmd);
            for (String arg : args) {
                sb.append(" '").append(arg.replace("'", "'\\''")).append("'");
            }
            Shell.Result result = Shell.cmd(sb.toString()).exec();
            if (result.isSuccess()) {
                return new ActionResult(actionType, true);
            } else {
                ActionResult r = new ActionResult(actionType, false);
                r.setLogs(TextUtils.join("\n", result.getErr()));
                return r;
            }
        }
        return null;
    }

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
        ActionResult privilegedResult = runPrivileged(ActionResult.TYPE_DELETE, "settings", "delete", settingsType, keyName);
        if (privilegedResult != null) return privilegedResult;

        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) {
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs("Shizuku/Permission request in progress...");
            return r;
        }
        if (!isGranted) {
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
        ActionResult privilegedResult = runPrivileged(actionType, "settings", "put", settingsType, keyName, newValue);
        if (privilegedResult != null) return privilegedResult;

        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) {
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs("Shizuku/Permission request in progress...");
            return r;
        }
        if (!isGranted) {
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
