package io.github.muntashirakon.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

public class UiUtilsTest {

    @Test
    public void testDpToPxInt() {
        Context context = Mockito.mock(Context.class);
        Resources resources = Mockito.mock(Resources.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        try (MockedStatic<TypedValue> mockedTypedValue = Mockito.mockStatic(TypedValue.class)) {
            mockedTypedValue.when(() -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics))
                .thenReturn(20f);

            int result = UiUtils.dpToPx(context, 10);
            assertEquals(20, result);
        }
    }

    @Test
    public void testDpToPxFloat() {
        Context context = Mockito.mock(Context.class);
        Resources resources = Mockito.mock(Resources.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        try (MockedStatic<TypedValue> mockedTypedValue = Mockito.mockStatic(TypedValue.class)) {
            mockedTypedValue.when(() -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, displayMetrics))
                .thenReturn(30f);

            int result = UiUtils.dpToPx(context, 15f);
            assertEquals(30, result);
        }
    }

    @Test
    public void testSpToPx() {
        Context context = Mockito.mock(Context.class);
        Resources resources = Mockito.mock(Resources.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        try (MockedStatic<TypedValue> mockedTypedValue = Mockito.mockStatic(TypedValue.class)) {
            mockedTypedValue.when(() -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, displayMetrics))
                .thenReturn(24f);

            int result = UiUtils.spToPx(context, 12f);
            assertEquals(24, result);
        }
    }

    @Test
    public void testPxToDp() {
        Context context = Mockito.mock(Context.class);
        Resources resources = Mockito.mock(Resources.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        displayMetrics.density = 2.0f;

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

        int result = UiUtils.pxToDp(context, 20);
        assertEquals(10, result);
    }
}
