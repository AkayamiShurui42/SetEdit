package io.github.muntashirakon.util;

<<<<<<< test-uiutils-pxtodp-8515587305831065790
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import org.junit.Test;
=======
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
>>>>>>> master

public class UiUtilsTest {

    @Test
<<<<<<< test-uiutils-pxtodp-8515587305831065790
    public void pxToDp_convertsCorrectly() {
        // Arrange
        Context mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 2.0f; // 2 pixels = 1 dp

        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, 10);

        // Assert
        assertEquals("10px should convert to 5dp at 2.0 density", 5, resultDp);
    }

    @Test
    public void pxToDp_convertsZero() {
        // Arrange
        Context mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 2.0f;

        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, 0);

        // Assert
        assertEquals("0px should convert to 0dp", 0, resultDp);
    }

    @Test
    public void pxToDp_convertsNegative() {
        // Arrange
        Context mockContext = mock(Context.class);
        Resources mockResources = mock(Resources.class);
        DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.density = 1.5f;

        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);

        // Act
        int resultDp = UiUtils.pxToDp(mockContext, -30);

        // Assert
        assertEquals("-30px should convert to -20dp at 1.5 density", -20, resultDp);
=======
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
>>>>>>> master
    }
}
