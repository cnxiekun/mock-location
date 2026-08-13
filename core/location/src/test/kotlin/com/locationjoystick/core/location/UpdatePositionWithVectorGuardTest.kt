package com.locationjoystick.core.location

import com.locationjoystick.core.data.LocationRepository
import com.locationjoystick.core.model.MockMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression test: [MockLocationService.updatePositionWithVector] is the entry point both
 * [JoystickOverlayService][com.locationjoystick.feature.joystick.impl.JoystickOverlayService]
 * and WalkCoordinator's position callback write through. It must be a no-op whenever an engine
 * other than those two owns the current tick's position (route replay, roaming, follower sync),
 * not just during FOLLOWER.
 */
class UpdatePositionWithVectorGuardTest {
    private fun newService(): MockLocationService =
        MockLocationService().apply {
            locationRepository = LocationRepository()
        }

    @Test
    fun `updatePositionWithVector applies while in JOYSTICK mode`() {
        val service = newService()
        service.locationRepository.setMockMode(MockMode.JOYSTICK)

        service.updatePositionWithVector(1.0, 2.0, speedMs = 3.5f, bearing = 90f)

        assertEquals(1.0, service.getCurrentPosition().latitude, 0.0)
        assertEquals(2.0, service.getCurrentPosition().longitude, 0.0)
    }

    @Test
    fun `updatePositionWithVector is a no-op during ROUTE_REPLAY`() {
        val service = newService()
        service.locationRepository.setMockMode(MockMode.ROUTE_REPLAY)
        val before = service.getCurrentPosition()

        service.updatePositionWithVector(1.0, 2.0, speedMs = 3.5f, bearing = 90f)

        assertEquals(before, service.getCurrentPosition())
    }

    @Test
    fun `updatePositionWithVector applies while in WALK_TO mode`() {
        // WalkCoordinator's own ACTION_UPDATE_POSITION callback routes through this method too,
        // self-reporting WALK_TO — must stay allowed.
        val service = newService()
        service.locationRepository.setMockMode(MockMode.WALK_TO)

        service.updatePositionWithVector(1.0, 2.0, speedMs = 3.5f, bearing = 90f)

        assertEquals(1.0, service.getCurrentPosition().latitude, 0.0)
        assertEquals(2.0, service.getCurrentPosition().longitude, 0.0)
    }

    @Test
    fun `updatePositionWithVector is a no-op during ROAMING`() {
        val service = newService()
        service.locationRepository.setMockMode(MockMode.ROAMING)
        val before = service.getCurrentPosition()

        service.updatePositionWithVector(1.0, 2.0, speedMs = 3.5f, bearing = 90f)

        assertEquals(before, service.getCurrentPosition())
    }

    @Test
    fun `updatePositionWithVector is a no-op during FOLLOWER`() {
        val service = newService()
        service.locationRepository.setMockMode(MockMode.FOLLOWER)
        val before = service.getCurrentPosition()

        service.updatePositionWithVector(1.0, 2.0, speedMs = 3.5f, bearing = 90f)

        assertEquals(before, service.getCurrentPosition())
    }
}
