package io.github.muntashirakon.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.mockito.Mockito;
import org.mockito.MockedStatic;

public class UiUtilsTest {

    @Test
    public void pxToDp_convertsCorrectly() {
        // Arrange
        Context mockContext = Mockito.mock(Context.class);
        Resources mockResources = Mockito.mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = Mockito.mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 2.0f; // 2 pixels = 1 dp

        Mockito.when(mockContext.getResources()).thenReturn(mockResources);
        Mockito.when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, 10);

        // Assert
        assertEquals("10px should convert to 5dp at 2.0 density", 5, resultDp);
    }

    @Test
    public void pxToDp_convertsZero() {
        // Arrange
        Context mockContext = Mockito.mock(Context.class);
        Resources mockResources = Mockito.mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = Mockito.mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 2.0f;

        Mockito.when(mockContext.getResources()).thenReturn(mockResources);
        Mockito.when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, 0);

        // Assert
        assertEquals("0px should convert to 0dp", 0, resultDp);
    }

    @Test
    public void pxToDp_convertsNegative() {
        // Arrange
        Context mockContext = Mockito.mock(Context.class);
        Resources mockResources = Mockito.mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = Mockito.mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 1.5f;

        Mockito.when(mockContext.getResources()).thenReturn(mockResources);
        Mockito.when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, -30);

        // Assert
        assertEquals("-30px should convert to -20dp at 1.5 density", -20, resultDp);
    }

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
    public void testIsDarkModeOnSystem_Yes() {
        Resources mockResources = Mockito.mock(Resources.class);
        Configuration mockConfig = new Configuration();
        mockConfig.uiMode = Configuration.UI_MODE_NIGHT_YES;
        Mockito.when(mockResources.getConfiguration()).thenReturn(mockConfig);

        try (MockedStatic<Resources> mockedResources = Mockito.mockStatic(Resources.class)) {
            mockedResources.when(Resources::getSystem).thenReturn(mockResources);
            assertTrue(UiUtils.isDarkModeOnSystem());
        }
    }

    @Test
    public void testIsDarkModeOnSystem_No() {
        Resources mockResources = Mockito.mock(Resources.class);
        Configuration mockConfig = new Configuration();
        mockConfig.uiMode = Configuration.UI_MODE_NIGHT_NO;
        Mockito.when(mockResources.getConfiguration()).thenReturn(mockConfig);

        try (MockedStatic<Resources> mockedResources = Mockito.mockStatic(Resources.class)) {
            mockedResources.when(Resources::getSystem).thenReturn(mockResources);
            assertFalse(UiUtils.isDarkModeOnSystem());
        }
    }

    @Test
    public void testIsDarkModeOnSystem_Undefined() {
        Resources mockResources = Mockito.mock(Resources.class);
        Configuration mockConfig = new Configuration();
        mockConfig.uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED;
        Mockito.when(mockResources.getConfiguration()).thenReturn(mockConfig);

        try (MockedStatic<Resources> mockedResources = Mockito.mockStatic(Resources.class)) {
            mockedResources.when(Resources::getSystem).thenReturn(mockResources);
            assertFalse(UiUtils.isDarkModeOnSystem());
        }
    }
}
