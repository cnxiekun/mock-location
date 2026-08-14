package com.locationjoystick.core.routing

import com.locationjoystick.core.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteReplayEngineJumpTest {
    private val engine = RouteReplayEngine(RouteInterpolator())

    private val a = LatLng(0.0, 0.0)
    private val b = LatLng(0.001, 0.0)
    private val c = LatLng(0.002, 0.0)
    private val d = LatLng(0.003, 0.0)
    private val waypoints = listOf(a, b, c, d)

    @Test
    fun `jumpToNextWaypoint after start returns the second waypoint`() {
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = {}, onComplete = {})
        engine.pause()

        val target = engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {})

        assertEquals(b, target)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }

    @Test
    fun `jumpToNextWaypoint twice returns the third waypoint`() {
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = {}, onComplete = {})
        engine.pause()

        engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {})
        val target = engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {})

        assertEquals(c, target)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }

    @Test
    fun `jumpToNextWaypoint at the last waypoint is a no-op`() {
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = {}, onComplete = {})
        engine.pause()

        repeat(3) { engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {}) }
        val target = engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {})

        assertEquals(d, target)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }

    @Test
    fun `jumpToPreviousWaypoint at the first waypoint is a no-op`() {
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = {}, onComplete = {})
        engine.pause()

        val target = engine.jumpToPreviousWaypoint(onPositionUpdate = {}, onComplete = {})

        assertEquals(a, target)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }

    @Test
    fun `jumpToPreviousWaypoint after reaching the end walks back through every waypoint`() {
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = {}, onComplete = {})
        engine.pause()
        repeat(3) { engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {}) }

        val first = engine.jumpToPreviousWaypoint(onPositionUpdate = {}, onComplete = {})
        val second = engine.jumpToPreviousWaypoint(onPositionUpdate = {}, onComplete = {})
        val third = engine.jumpToPreviousWaypoint(onPositionUpdate = {}, onComplete = {})

        assertEquals(c, first)
        assertEquals(b, second)
        assertEquals(a, third)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }

    @Test
    fun `jumpToNextWaypoint with no active replay returns null`() {
        val target = engine.jumpToNextWaypoint(onPositionUpdate = {}, onComplete = {})

        assertNull(target)
    }

    @Test
    fun `jumpToNextWaypoint while running resumes ticking toward the following waypoint`() {
        val positions = mutableListOf<LatLng>()
        engine.start(waypoints = waypoints, speedMs = 1.4, onPositionUpdate = { pos -> positions.add(pos) }, onComplete = {})

        Thread.sleep(200)
        engine.jumpToNextWaypoint(onPositionUpdate = { pos -> positions.add(pos) }, onComplete = {})
        val countAfterJump = positions.size

        Thread.sleep(1500)

        assertTrue("should have more positions after jump", positions.size > countAfterJump)
        kotlinx.coroutines.runBlocking { engine.stop() }
    }
}
