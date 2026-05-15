package io.github.muntashirakon.setedit.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

import com.topjohnwu.superuser.Shell;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AndroidPropertyUtilsTest {
    @Test
    public void testUpdateSuccess() {
        try (MockedStatic<Shell> shellMock = Mockito.mockStatic(Shell.class);
             MockedStatic<TextUtils> textUtilsMock = Mockito.mockStatic(TextUtils.class)) {

            Shell.Job job = mock(Shell.Job.class);
            Shell.Result result = mock(Shell.Result.class);

            shellMock.when(() -> Shell.cmd("resetprop my_key \"my_value\"")).thenReturn(job);
            when(job.exec()).thenReturn(result);
            when(result.isSuccess()).thenReturn(true);
            when(result.getErr()).thenReturn(Collections.emptyList());

            textUtilsMock.when(() -> TextUtils.join("\n", Collections.emptyList())).thenReturn("");

            ActionResult actionResult = AndroidPropertyUtils.update("my_key", "my_value");

            assertNotNull(actionResult);
            assertEquals(ActionResult.TYPE_UPDATE, actionResult.type);
            assertTrue(actionResult.successful);
            assertEquals("", actionResult.getLogs());
        }
    }

    @Test
    public void testUpdateFailure() {
        try (MockedStatic<Shell> shellMock = Mockito.mockStatic(Shell.class);
             MockedStatic<TextUtils> textUtilsMock = Mockito.mockStatic(TextUtils.class)) {

            Shell.Job job = mock(Shell.Job.class);
            Shell.Result result = mock(Shell.Result.class);

            shellMock.when(() -> Shell.cmd("resetprop bad_key \"bad_value\"")).thenReturn(job);
            when(job.exec()).thenReturn(result);
            when(result.isSuccess()).thenReturn(false);
            when(result.getErr()).thenReturn(Arrays.asList("Error 1", "Error 2"));

            textUtilsMock.when(() -> TextUtils.join("\n", Arrays.asList("Error 1", "Error 2"))).thenReturn("Error 1\nError 2");

            ActionResult actionResult = AndroidPropertyUtils.update("bad_key", "bad_value");

            assertNotNull(actionResult);
            assertEquals(ActionResult.TYPE_UPDATE, actionResult.type);
            assertFalse(actionResult.successful);
            assertEquals("Error 1\nError 2", actionResult.getLogs());
        }
    }
}
