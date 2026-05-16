package io.github.muntashirakon.setedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import org.json.JSONException;
import org.json.JSONObject;

import rikka.shizuku.Shizuku;

import java.util.List;

public class EditorUtils {
    public static final int REQUEST_CODE_SHIZUKU = 1001;

    public static rikka.shizuku.ShizukuRemoteProcess newShizukuProcess(String[] cmd, String[] env, String dir) throws Exception {
        java.lang.reflect.Method method = rikka.shizuku.Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
        method.setAccessible(true);
        return (rikka.shizuku.ShizukuRemoteProcess) method.invoke(null, cmd, env, dir);
    }

    /**
     * Check whether the permission has been granted
     *
     * @return {@code true} if granted, {@code null} if is being granted and {@code false} otherwise
     */
    @Nullable
    public static Boolean checkSettingsPermission(@NonNull Context context, @SettingsType String settingsType) {
        String permission = SettingsType.SYSTEM_SETTINGS.equals(settingsType)
                ? Manifest.permission.WRITE_SETTINGS : Manifest.permission.WRITE_SECURE_SETTINGS;
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else if (context instanceof android.app.Activity) {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU);
                return null;
            }
        }
        if (SettingsType.SYSTEM_SETTINGS.equals(settingsType)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
                    Shell.cmd("appops set " + Process.myUid() + " 23 0",
                            "appops set " + BuildConfig.APPLICATION_ID + " 23 0").exec();
                }
                if (!Settings.System.canWrite(context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        if (!(context instanceof android.app.Activity)) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        } else if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.cmd("pm grant " + BuildConfig.APPLICATION_ID + " " + permission).exec();
        } else if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            try {
                newShizukuProcess(new String[]{"pm", "grant", BuildConfig.APPLICATION_ID, permission}, null, null).waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void displayGrantPermissionMessage(@NonNull Context context) {
        // Disabled as requested by user to remove disruptive popups.
    }

    @NonNull
    public static String getJson(@NonNull List<Pair<String, String>> items, @Nullable String settingsType)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (settingsType != null) {
            jsonObject.put("_settings_type", settingsType);
        }
        for (Pair<String, String> pair : items) {
            jsonObject.put(pair.first, pair.second);
        }
        return jsonObject.toString(4);
    }

    @TableType
    public static String toTableType(@TableTypeInt int tableTypeInt) {
        switch (tableTypeInt) {
            case TableTypeInt.TABLE_SYSTEM:
                return TableType.TABLE_SYSTEM;
            case TableTypeInt.TABLE_SECURE:
                return TableType.TABLE_SECURE;
            case TableTypeInt.TABLE_GLOBAL:
                return TableType.TABLE_GLOBAL;
            case TableTypeInt.TABLE_PROPERTIES:
                return TableType.TABLE_PROPERTIES;
            case TableTypeInt.TABLE_JAVA:
                return TableType.TABLE_JAVA;
            case TableTypeInt.TABLE_ENV:
                return TableType.TABLE_ENV;
            case TableTypeInt.TABLE_BOOT_AND_LOCKED:
                return TableType.TABLE_BOOT_AND_LOCKED;
            case TableTypeInt.TABLE_SHORTCUTS:
                return TableType.TABLE_SHORTCUTS;
            default:
                throw new IllegalArgumentException("Invalid table type: " + tableTypeInt);
        }
    }

    public static void requestAllPermissions(@NonNull Context context) {
        checkSettingsPermission(context, SettingsType.SYSTEM_SETTINGS);
        checkSettingsPermission(context, SettingsType.SECURE_SETTINGS);
    }
}
