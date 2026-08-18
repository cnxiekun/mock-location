package com.locationjoystick.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CooldownStateTest {
    @Test
    fun `toAdvisoryLabel shows seconds when under one minute`() {
        val label = CooldownState.Cooling(45L, 60L, 500.0).toAdvisoryLabel()
        assertEquals("45秒 · 500 米 传送", label)
    }

    @Test
    fun `toAdvisoryLabel shows minutes and seconds when under one hour`() {
        val label = CooldownState.Cooling(125L, 300L, 500.0).toAdvisoryLabel()
        assertEquals("2分 5秒 · 500 米 传送", label)
    }

    @Test
    fun `toAdvisoryLabel shows hours and minutes when one hour or more`() {
        val label = CooldownState.Cooling(3725L, 7200L, 500.0).toAdvisoryLabel()
        assertEquals("1小时 2分 · 500 米 传送", label)
    }

    @Test
    fun `toAdvisoryLabel shows km when distance is 1000m or more`() {
        val label = CooldownState.Cooling(30L, 60L, 1000.0).toAdvisoryLabel()
        assertEquals("30秒 · 1.0 公里 传送", label)
    }

    @Test
    fun `toAdvisoryLabel shows km with decimal for large distance`() {
        val label = CooldownState.Cooling(30L, 60L, 2500.0).toAdvisoryLabel()
        assertEquals("30秒 · 2.5 公里 传送", label)
    }

    @Test
    fun `toAdvisoryLabel shows meters when distance under 1000m`() {
        val label = CooldownState.Cooling(30L, 60L, 999.0).toAdvisoryLabel()
        assertEquals("30秒 · 999 米 传送", label)
    }

    @Test
    fun `toAdvisoryLabel boundary 60s shows minutes not seconds`() {
        val label = CooldownState.Cooling(60L, 60L, 100.0).toAdvisoryLabel()
        assertEquals("1分 0秒 · 100 米 传送", label)
    }

    @Test
    fun `toAdvisoryLabel boundary 3600s shows hours not minutes`() {
        val label = CooldownState.Cooling(3600L, 7200L, 100.0).toAdvisoryLabel()
        assertEquals("1小时 0分 · 100 米 传送", label)
    }
}
