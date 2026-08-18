package com.locationjoystick.core.designsystem.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.common.util.Gcj02Converter
import com.locationjoystick.core.common.util.parseRawLatLng
import com.locationjoystick.core.model.RecentSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "NominatimSearchBar"

/**
 * 地图搜索栏：点击搜索按钮或键盘搜索键才真正发起搜索（不是边输入边搜）。
 * 使用高德地理编码（需要设置中填写的 amapKey）。
 */
@Composable
fun NominatimSearchBar(
    onLocationSelected: (lat: Double, lon: Double, displayName: String) -> Unit,
    modifier: Modifier = Modifier,
    recentSearches: List<RecentSearch> = emptyList(),
    onSearchCommitted: ((displayName: String, lat: Double, lon: Double) -> Unit)? = null,
    amapKey: String = "",
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun performSearch() {
        val q = query
        hasSearched = true
        val rawCoords = parseRawLatLng(q)
        if (rawCoords != null) {
            results =
                listOf(
                    NominatimResult(
                        lat = rawCoords.latitude,
                        lon = rawCoords.longitude,
                        displayName = "前往 ${rawCoords.latitude}, ${rawCoords.longitude}",
                    ),
                )
            return
        }
        if (q.length < 2) {
            results = emptyList()
            return
        }
        if (amapKey.isBlank()) {
            results = emptyList()
            return
        }
        isLoading = true
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val encoded = URLEncoder.encode(q, "UTF-8")
                    val url = URL("${AppConstants.AmapConstants.GEOCODE_URL}?address=$encoded&key=$amapKey")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = AppConstants.NominatimConstants.CONNECT_TIMEOUT_MS
                    conn.readTimeout = AppConstants.NominatimConstants.READ_TIMEOUT_MS
                    try {
                        val responseText = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(responseText)
                        val geocodes = json.optJSONArray("geocodes") ?: JSONArray()
                        val parsed =
                            (0 until minOf(geocodes.length(), 5)).mapNotNull { i ->
                                try {
                                    val obj = geocodes.getJSONObject(i)
                                    val location = obj.optString("location") // "lon,lat"（GCJ-02）
                                    val parts = location.split(",")
                                    val lon = parts.getOrNull(0)?.toDoubleOrNull() ?: return@mapNotNull null
                                    val lat = parts.getOrNull(1)?.toDoubleOrNull() ?: return@mapNotNull null
                                    // 高德返回 GCJ-02 火星坐标，转回 WGS-84 存储
                                    val wgs = Gcj02Converter.gcj02ToWgs84(lat, lon)
                                    NominatimResult(
                                        lat = wgs.latitude,
                                        lon = wgs.longitude,
                                        displayName = obj.optString("formatted_address", q),
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        results = parsed
                    } finally {
                        conn.disconnect()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Search failed", e)
                    results = emptyList()
                }
            }
            isLoading = false
        }
    }

    val showRecent = query.isEmpty() && recentSearches.isNotEmpty()
    val showResults = showRecent || hasSearched

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                hasSearched = false
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
            placeholder = { Text("搜索位置...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { performSearch() }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch() }),
            shape =
                if (showResults) {
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                } else {
                    RoundedCornerShape(12.dp)
                },
        )

        if (showResults) {
            HorizontalDivider()
            if (showRecent) {
                Text(
                    text = "最近搜索",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                recentSearches.forEach { recent ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(recent.lat, recent.lon, recent.displayName)
                                    onSearchCommitted?.invoke(recent.displayName, recent.lat, recent.lon)
                                    query = ""
                                }.padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = recent.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    HorizontalDivider()
                }
            }
            if (hasSearched && results.isEmpty() && !isLoading && !showRecent) {
                Text(
                    text = if (amapKey.isBlank()) "请在设置中填写高德 API Key" else "未找到结果",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            results.forEach { result ->
                Text(
                    text = result.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLocationSelected(result.lat, result.lon, result.displayName)
                                onSearchCommitted?.invoke(result.displayName, result.lat, result.lon)
                                query = ""
                                results = emptyList()
                                hasSearched = false
                            }.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                HorizontalDivider()
            }
        }
    }
}

private data class NominatimResult(
    val lat: Double,
    val lon: Double,
    val displayName: String,
)
