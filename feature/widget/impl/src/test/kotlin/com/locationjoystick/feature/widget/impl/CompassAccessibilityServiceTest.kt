package com.locationjoystick.feature.widget.impl

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassAccessibilityServiceTest {
    @Test
    fun belowApi30_unsupported() {
        assertFalse(CompassAccessibilityService.isSupported(Build.VERSION_CODES.Q))
    }

    @Test
    fun api30AndAbove_supported() {
        assertTrue(CompassAccessibilityService.isSupported(Build.VERSION_CODES.R))
        assertTrue(CompassAccessibilityService.isSupported(Build.VERSION_CODES.TIRAMISU))
    }
}
