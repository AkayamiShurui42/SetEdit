package io.github.muntashirakon.setedit.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import rikka.shizuku.Shizuku;

public final class AndroidPropertyUtils {
    @NonNull
    public static ActionResult update(@NonNull String keyName, @NonNull String newValue) {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                String cmd = "setprop " + keyName + " \"" + newValue + "\"";
                java.lang.Process process = Shizuku.newProcess(new String[]{"sh", "-c", cmd}, null, null);
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return new ActionResult(ActionResult.TYPE_UPDATE, true);
                } else {
                    ActionResult r = new ActionResult(ActionResult.TYPE_UPDATE, false);
                    r.setLogs("Shizuku setprop failed with exit code: " + exitCode);
                    return r;
                }
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
