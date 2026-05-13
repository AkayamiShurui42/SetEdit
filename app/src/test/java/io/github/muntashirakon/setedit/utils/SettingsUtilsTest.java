package io.github.muntashirakon.setedit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import com.topjohnwu.superuser.Shell;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.SettingsType;
import rikka.shizuku.Shizuku;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SettingsUtilsTest {

    private Context context;
    private ContentResolver contentResolver;
    private MockedStatic<Shell> shellMock;
    private MockedStatic<Shizuku> shizukuMock;
    private MockedStatic<EditorUtils> editorUtilsMock;

    @Before
    public void setUp() {
        context = mock(Context.class);
        contentResolver = mock(ContentResolver.class);
        when(context.getContentResolver()).thenReturn(contentResolver);

        shellMock = Mockito.mockStatic(Shell.class);
        shizukuMock = Mockito.mockStatic(Shizuku.class);
        editorUtilsMock = Mockito.mockStatic(EditorUtils.class);
    }

    @After
    public void tearDown() {
        if (shellMock != null) {
            shellMock.close();
        }
        if (shizukuMock != null) {
            shizukuMock.close();
        }
        if (editorUtilsMock != null) {
            editorUtilsMock.close();
        }
    }

    @Test
    public void testUpdate_rootSuccess() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.TRUE);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(true);
        when(mockResult.getErr()).thenReturn(new java.util.ArrayList<>());

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("settings put system test_key \"test_value\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertTrue(result.successful);
    }

    @Test
    public void testUpdate_rootFailure() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.TRUE);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(false);
        java.util.List<String> errList = new java.util.ArrayList<>();
        errList.add("Error line 1");
        errList.add("Error line 2");
        when(mockResult.getErr()).thenReturn(errList);

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("settings put system test_key \"test_value\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
        assertEquals("Error line 1\nError line 2", result.getLogs());
    }

    @Test
    public void testUpdate_shizukuSuccess() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(true);
        shizukuMock.when(Shizuku::checkSelfPermission).thenReturn(android.content.pm.PackageManager.PERMISSION_GRANTED);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(true);

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings put system test_key \"test_value\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertTrue(result.successful);
    }

    @Test
    public void testUpdate_shizukuFailure() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(true);
        shizukuMock.when(Shizuku::checkSelfPermission).thenReturn(android.content.pm.PackageManager.PERMISSION_GRANTED);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(false);

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings put system test_key \"test_value\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
    }

    @Test
    public void testUpdate_shizukuException() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(true);
        shizukuMock.when(Shizuku::checkSelfPermission).thenReturn(android.content.pm.PackageManager.PERMISSION_GRANTED);

        shellMock.when(() -> Shell.cmd("app_process -Djava.class.path=/data/local/tmp/shizuku/shizuku.apk /system/bin com.android.commands.settings.Settings put system test_key \"test_value\"")).thenThrow(new RuntimeException("Shell failed"));

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
        assertEquals("Shell failed", result.getLogs());
    }

    @Test
    public void testUpdate_contentResolverSuccess() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(false);

        editorUtilsMock.when(() -> EditorUtils.checkSettingsPermission(context, SettingsType.SYSTEM_SETTINGS)).thenReturn(true);

        // Mock successful insert (returns a non-null Uri on success, but insert itself just returns Uri, no exception thrown)
        Uri mockInsertUri = mock(Uri.class);
        when(contentResolver.insert(any(Uri.class), any(ContentValues.class)))
            .thenReturn(mockInsertUri);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertTrue(result.successful);

        ArgumentCaptor<ContentValues> captor = ArgumentCaptor.forClass(ContentValues.class);
        verify(contentResolver).insert(eq(Uri.parse("content://settings/system")), captor.capture());
        ContentValues values = captor.getValue();
        assertEquals("test_key", values.get("name"));
        assertEquals("test_value", values.get("value"));
    }

    @Test
    public void testUpdate_contentResolverPermissionDenied() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(false);

        editorUtilsMock.when(() -> EditorUtils.checkSettingsPermission(context, SettingsType.SYSTEM_SETTINGS)).thenReturn(false);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
    }

    @Test
    public void testUpdate_contentResolverPermissionNull() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(false);

        editorUtilsMock.when(() -> EditorUtils.checkSettingsPermission(context, SettingsType.SYSTEM_SETTINGS)).thenReturn(null);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
    }

    @Test
    public void testUpdate_contentResolverException() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.FALSE);
        shizukuMock.when(Shizuku::pingBinder).thenReturn(false);

        editorUtilsMock.when(() -> EditorUtils.checkSettingsPermission(context, SettingsType.SYSTEM_SETTINGS)).thenReturn(true);

        when(contentResolver.insert(any(Uri.class), any(ContentValues.class)))
            .thenThrow(new SecurityException("Permission denied"));

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertFalse(result.successful);
        assertEquals("Permission denied", result.getLogs());
    }

    @Test
    public void testUpdate_nullValueClearsValue() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.TRUE);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(true);
        when(mockResult.getErr()).thenReturn(new java.util.ArrayList<>());

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("settings put system test_key \"\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.update(context, SettingsType.SYSTEM_SETTINGS, "test_key", "null");

        assertEquals(ActionResult.TYPE_UPDATE, result.type);
        assertTrue(result.successful);
    }

    @Test
    public void testCreate_usesCreateActionType() {
        shellMock.when(Shell::isAppGrantedRoot).thenReturn(Boolean.TRUE);

        Shell.Result mockResult = mock(Shell.Result.class);
        when(mockResult.isSuccess()).thenReturn(true);
        when(mockResult.getErr()).thenReturn(new java.util.ArrayList<>());

        Shell.Job mockJob = mock(Shell.Job.class);
        when(mockJob.exec()).thenReturn(mockResult);

        shellMock.when(() -> Shell.cmd("settings put system test_key \"test_value\"")).thenReturn(mockJob);

        ActionResult result = SettingsUtils.create(context, SettingsType.SYSTEM_SETTINGS, "test_key", "test_value");

        assertEquals(ActionResult.TYPE_CREATE, result.type);
        assertTrue(result.successful);
    }
}
