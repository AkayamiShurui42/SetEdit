package io.github.muntashirakon.setedit.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.SettingsType;
import rikka.shizuku.Shizuku;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SettingsUtilsTest {

    private MockedStatic<Shell> shellMockedStatic;
    private MockedStatic<Shizuku> shizukuMockedStatic;
    private MockedStatic<EditorUtils> editorUtilsMockedStatic;

    @Mock
    private Context context;

    @Mock
    private Activity activityContext;

    @Mock
    private ContentResolver contentResolver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        shellMockedStatic = mockStatic(Shell.class);
        shizukuMockedStatic = mockStatic(Shizuku.class);
        editorUtilsMockedStatic = mockStatic(EditorUtils.class);

        when(context.getContentResolver()).thenReturn(contentResolver);
        when(activityContext.getContentResolver()).thenReturn(contentResolver);
    }

    @After
    public void tearDown() {
        if (shellMockedStatic != null) {
            shellMockedStatic.close();
        }
        if (shizukuMockedStatic != null) {
            shizukuMockedStatic.close();
        }
        if (editorUtilsMockedStatic != null) {
            editorUtilsMockedStatic.close();
        }
    }

    // Helper method to setup Shell mock
    private void setupShellRoot(boolean isRootGranted, boolean isSuccess) {
        shellMockedStatic.when(Shell::isAppGrantedRoot).thenReturn(isRootGranted);
        if (isRootGranted) {
            Shell.Job job = mock(Shell.Job.class);
            Shell.Result result = mock(Shell.Result.class);

            shellMockedStatic.when(() -> Shell.cmd(anyString())).thenReturn(job);
            when(job.exec()).thenReturn(result);
            when(result.isSuccess()).thenReturn(isSuccess);
            when(result.getErr()).thenReturn(new ArrayList<>());
        } else {
            shellMockedStatic.when(Shell::isAppGrantedRoot).thenReturn(false);
        }
    }

    // Helper method to setup Shizuku mock
    private void setupShizuku(boolean pingBinder, boolean hasPermission, boolean isSuccess) {
        shizukuMockedStatic.when(Shizuku::pingBinder).thenReturn(pingBinder);
        shizukuMockedStatic.when(Shizuku::checkSelfPermission).thenReturn(
                hasPermission ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED
        );
        if (pingBinder && hasPermission) {
            // Only set this up if we aren't throwing an exception later in the test
            Shell.Job job = mock(Shell.Job.class);
            Shell.Result result = mock(Shell.Result.class);

            shellMockedStatic.when(() -> Shell.cmd(anyString())).thenReturn(job);
            when(job.exec()).thenReturn(result);
            when(result.isSuccess()).thenReturn(isSuccess);
            when(result.getErr()).thenReturn(new ArrayList<>());
        }
    }

    // --- Delete Tests ---

    @Test
    public void delete_withRoot_success() {
        setupShellRoot(true, true);

        ActionResult result = SettingsUtils.delete(context, SettingsType.SYSTEM_SETTINGS, "test_key");

        assertTrue(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        shellMockedStatic.verify(() -> Shell.cmd("settings delete system test_key"));
    }

    @Test
    public void delete_withRoot_failure() {
        setupShellRoot(true, false);

        ActionResult result = SettingsUtils.delete(context, SettingsType.SYSTEM_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
    }

    @Test
    public void delete_withShizuku_success() {
        setupShellRoot(false, false);
        setupShizuku(true, true, true);

        ActionResult result = SettingsUtils.delete(context, SettingsType.SECURE_SETTINGS, "test_key");

        assertTrue(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        shellMockedStatic.verify(() -> Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings delete secure test_key"));
    }

    @Test
    public void delete_withShizuku_failure() {
        setupShellRoot(false, false);
        setupShizuku(true, true, false);

        ActionResult result = SettingsUtils.delete(context, SettingsType.SECURE_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
    }

    @Test
    public void delete_withShizuku_exception() {
        setupShellRoot(false, false);
        shizukuMockedStatic.when(Shizuku::pingBinder).thenReturn(true);
        shizukuMockedStatic.when(Shizuku::checkSelfPermission).thenReturn(PackageManager.PERMISSION_GRANTED);

        Shell.Job job = mock(Shell.Job.class);
        shellMockedStatic.when(() -> Shell.cmd(anyString())).thenReturn(job);
        when(job.exec()).thenThrow(new RuntimeException("Shizuku error"));

        ActionResult result = SettingsUtils.delete(context, SettingsType.SECURE_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        assertEquals("Shizuku error", result.getLogs());
    }

    @Test
    public void delete_withContentResolver_success() {
        setupShellRoot(false, false);
        setupShizuku(false, false, false);
        editorUtilsMockedStatic.when(() -> EditorUtils.checkSettingsPermission(any(), anyString())).thenReturn(true);

        when(contentResolver.delete(any(Uri.class), anyString(), any())).thenReturn(1);

        ActionResult result = SettingsUtils.delete(context, SettingsType.GLOBAL_SETTINGS, "test_key");

        assertTrue(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        verify(contentResolver).delete(eq(Uri.parse("content://settings/global")), eq("name = ?"), eq(new String[]{"test_key"}));
    }

    @Test
    public void delete_withContentResolver_noPermission_null() {
        setupShellRoot(false, false);
        setupShizuku(false, false, false);
        editorUtilsMockedStatic.when(() -> EditorUtils.checkSettingsPermission(any(), anyString())).thenReturn(null);

        ActionResult result = SettingsUtils.delete(context, SettingsType.GLOBAL_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
    }

    @Test
    public void delete_withContentResolver_noPermission_false() {
        setupShellRoot(false, false);
        setupShizuku(false, false, false);
        editorUtilsMockedStatic.when(() -> EditorUtils.checkSettingsPermission(any(), anyString())).thenReturn(false);

        ActionResult result = SettingsUtils.delete(context, SettingsType.GLOBAL_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
    }

    @Test
    public void delete_withContentResolver_noPermission_false_withActivity() {
        setupShellRoot(false, false);
        setupShizuku(false, false, false);
        editorUtilsMockedStatic.when(() -> EditorUtils.checkSettingsPermission(any(), anyString())).thenReturn(false);

        ActionResult result = SettingsUtils.delete(activityContext, SettingsType.GLOBAL_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        editorUtilsMockedStatic.verify(() -> EditorUtils.displayGrantPermissionMessage(activityContext));
    }

    @Test
    public void delete_withContentResolver_exception() {
        setupShellRoot(false, false);
        setupShizuku(false, false, false);
        editorUtilsMockedStatic.when(() -> EditorUtils.checkSettingsPermission(any(), anyString())).thenReturn(true);

        when(contentResolver.delete(any(Uri.class), anyString(), any())).thenThrow(new IllegalArgumentException("Resolver error"));

        ActionResult result = SettingsUtils.delete(context, SettingsType.GLOBAL_SETTINGS, "test_key");

        assertFalse(result.successful);
        assertEquals(ActionResult.TYPE_DELETE, result.type);
        assertEquals("Resolver error", result.getLogs());
    }
}
