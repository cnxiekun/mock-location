package com.locationjoystick.core.location

import com.locationjoystick.core.common.constants.AppConstants
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Direct unit tests for the [shouldPersistLastLocation] throttle used by pushLocationUpdate(). */
class ShouldPersistLastLocationTest {
    private val intervalMs = AppConstants.LocationConstants.LAST_LOCATION_PERSIST_INTERVAL_MS

    @Test
    fun `skips write when interval has not elapsed`() {
        assertFalse(shouldPersistLastLocation(lastPersistedMs = 1000L, nowMs = 1000L + intervalMs - 1))
    }

    @Test
    fun `writes once interval has elapsed`() {
        assertTrue(shouldPersistLastLocation(lastPersistedMs = 1000L, nowMs = 1000L + intervalMs))
    }

    @Test
    fun `writes on the very first tick since boot elapsed time already exceeds the interval`() {
        assertTrue(shouldPersistLastLocation(lastPersistedMs = 0L, nowMs = intervalMs))
    }
}
