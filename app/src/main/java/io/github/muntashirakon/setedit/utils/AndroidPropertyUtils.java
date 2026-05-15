package io.github.muntashirakon.setedit.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import rikka.shizuku.Shizuku;

public final class AndroidPropertyUtils {
    @NonNull
    public static ActionResult update(@NonNull String keyName, @NonNull String newValue) {
        if (rikka.shizuku.Shizuku.pingBinder() && rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                // Try using shizuku binary directly first
                Shell.Result result = Shell.cmd("shizuku -c 'setprop " + keyName + " \"" + newValue + "\"'").exec();
                if (!result.isSuccess()) {
                    // Fallback to app_process hack
                    result = Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin setprop " + keyName + " \"" + newValue + "\"").exec();
                }
                return new ActionResult(ActionResult.TYPE_UPDATE, result.isSuccess());
            } catch (Exception e) {
                ActionResult r = new ActionResult(ActionResult.TYPE_UPDATE, false);
                r.setLogs(e.getMessage());
                return r;
            }
        }
        Shell.Result result = Shell.cmd("resetprop " + keyName + " \"" + newValue + "\"").exec();
        ActionResult r = new ActionResult(ActionResult.TYPE_UPDATE, result.isSuccess());
        r.setLogs(TextUtils.join("\n", result.getErr()));
        return r;
    }
}
