package com.locationjoystick.core.common.util

import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface

private const val TAG = "NetworkUtils"

object NetworkUtils {
    /** Returns this device's local Wi-Fi/LAN IPv4 address, or null if none is found. */
    fun getLocalIpAddress(): String? =
        try {
            val candidates =
                NetworkInterface
                    .getNetworkInterfaces()
                    ?.toList()
                    ?.flatMap { iface -> iface.inetAddresses.toList().map { iface.name to it } }
                    ?.filter { (_, addr) -> !addr.isLoopbackAddress && addr is Inet4Address }
                    .orEmpty()
            // Cellular (rmnet) can enumerate before Wi-Fi and hand out an unreachable CGNAT
            // address — prefer Wi-Fi client (wlan) or hotspot-host (ap/swlan) interfaces.
            val preferred =
                candidates.firstOrNull { (name, _) ->
                    name.startsWith("wlan") || name.startsWith("ap") || name.startsWith("swlan")
                } ?: candidates.firstOrNull { (_, addr) -> addr.isSiteLocalAddress }
                    ?: candidates.firstOrNull()
            preferred?.second?.hostAddress
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get local IP", e)
            null
        }
}
