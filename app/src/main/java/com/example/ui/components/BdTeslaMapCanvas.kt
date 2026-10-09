package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.localization.AppLanguage
import com.example.data.model.GeoPoint
import com.example.data.model.RideRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.concurrent.TimeUnit

private val routeHttpClient = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .callTimeout(15, TimeUnit.SECONDS)
    .build()

@Composable
fun BdTeslaMapCanvas(
    pickup: GeoPoint?,
    destination: GeoPoint?,
    activeRide: RideRequest?,
    language: AppLanguage,
    onRouteCalculated: (Double, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var routePoints by remember { mutableStateOf<List<OsmGeoPoint>>(emptyList()) }

    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(13.0)
            controller.setCenter(
                pickup?.let { OsmGeoPoint(it.latitude, it.longitude) }
                    ?: OsmGeoPoint(23.9026, 89.1221)
            )
            overlays.add(CopyrightOverlay(context))
        }
    }

    LaunchedEffect(pickup?.latitude, pickup?.longitude, destination?.latitude, destination?.longitude) {
        routePoints = emptyList()
        val start = pickup
        val end = destination
        if (start == null || end == null) return@LaunchedEffect

        val result = withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "BDTESLA-Android/1.0")
                    .build()
                routeHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IllegalStateException("Routing service returned ${response.code}")
                    val body = response.body?.string() ?: throw IllegalStateException("Empty routing response")
                    val routes = JSONObject(body).optJSONArray("routes")
                        ?: throw IllegalStateException("No route found")
                    if (routes.length() == 0) throw IllegalStateException("No route found")
                    val route = routes.getJSONObject(0)
                    val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")
                    val points = buildList {
                        for (i in 0 until coordinates.length()) {
                            val pair = coordinates.getJSONArray(i)
                            add(OsmGeoPoint(pair.getDouble(1), pair.getDouble(0)))
                        }
                    }
                    points to Pair(
                        route.optDouble("distance", 0.0) / 1000.0,
                        (route.optDouble("duration", 0.0) / 60.0).toInt().coerceAtLeast(1)
                    )
                }
            }.getOrNull()
        }

        if (result != null) {
            routePoints = result.first
            onRouteCalculated(result.second.first, result.second.second)
        }
    }

    LaunchedEffect(pickup?.latitude, pickup?.longitude, destination?.latitude, destination?.longitude) {
        val points = listOfNotNull(pickup, destination)
        if (points.isNotEmpty()) {
            val centerLat = points.map { it.latitude }.average()
            val centerLon = points.map { it.longitude }.average()
            mapView.controller.setCenter(OsmGeoPoint(centerLat, centerLon))
            mapView.controller.setZoom(if (points.size > 1) 13.0 else 15.0)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            map.overlays.clear()
            map.overlays.add(CopyrightOverlay(context))

            if (routePoints.size > 1) {
                val route = Polyline(map).apply {
                    setPoints(routePoints)
                    setColor(AndroidColor.rgb(0, 220, 130))
                    setWidth(9f)
                    title = if (language == AppLanguage.BANGLA) "যাত্রার রুট" else "Trip route"
                }
                map.overlays.add(route)
            }

            pickup?.let { point ->
                val marker = Marker(map).apply {
                    position = OsmGeoPoint(point.latitude, point.longitude)
                    title = if (language == AppLanguage.BANGLA) point.nameBn else point.nameEn
                    subDescription = if (language == AppLanguage.BANGLA) "যাত্রা শুরুর স্থান" else "Pickup"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(marker)
            }

            destination?.let { point ->
                val marker = Marker(map).apply {
                    position = OsmGeoPoint(point.latitude, point.longitude)
                    title = if (language == AppLanguage.BANGLA) point.nameBn else point.nameEn
                    subDescription = if (language == AppLanguage.BANGLA) "গন্তব্য" else "Destination"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(marker)
            }

            val driverPoint = activeRide?.driverLocation
            if (driverPoint != null && activeRide.driverId != null) {
                val marker = Marker(map).apply {
                    position = OsmGeoPoint(driverPoint.latitude, driverPoint.longitude)
                    title = activeRide.driverName ?: if (language == AppLanguage.BANGLA) "ড্রাইভার" else "Driver"
                    subDescription = activeRide.vehicleNumber
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(marker)
            }
            map.invalidate()
        }
    )

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }
}
