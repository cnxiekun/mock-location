package com.locationjoystick.core.common.util

import com.locationjoystick.core.model.LatLng

/**
 * WGS-84 ↔ GCJ-02（火星坐标）双向转换。
 *
 * 底图使用高德栅格瓦片（GCJ-02 坐标系），而模拟定位/收藏/搜索使用 WGS-84 坐标。
 * 显示标记与相机位置时调用 [wgs84ToGcj02]，读取地图点击坐标时调用 [gcj02ToWgs84]，
 * 使两者在地图上对齐。
 */
object Gcj02Converter {
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    fun wgs84ToGcj02(
        lat: Double,
        lon: Double,
    ): LatLng {
        if (outOfChina(lat, lon)) return LatLng(lat, lon)
        var dLat = transformLat(lon - 105.0, lat - 35.0)
        var dLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * Math.PI
        var magic = Math.sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = Math.sqrt(magic)
        dLat = dLat * 180.0 / ((A * (1 - EE)) / (magic * sqrtMagic) * Math.PI)
        dLon = dLon * 180.0 / (A / sqrtMagic * Math.cos(radLat) * Math.PI)
        return LatLng(lat + dLat, lon + dLon)
    }

    fun gcj02ToWgs84(
        lat: Double,
        lon: Double,
    ): LatLng {
        if (outOfChina(lat, lon)) return LatLng(lat, lon)
        val gcj = wgs84ToGcj02(lat, lon)
        return LatLng(lat * 2 - gcj.latitude, lon * 2 - gcj.longitude)
    }

    private fun transformLat(
        x: Double,
        y: Double,
    ): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLon(
        x: Double,
        y: Double,
    ): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
        return ret
    }

    private fun outOfChina(
        lat: Double,
        lon: Double,
    ): Boolean = lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271
}
