package com.river.walklog.core.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * FusedLocationProviderClient로 기기의 실제 위치를 조회.
 *
 * 1. 위치 권한 없음 → [LocaleWeatherLocationProvider] fallback
 * 2. 마지막 알려진 위치 없음 → 현재 위치 단건 요청
 * 3. Geocoder로 도시명-국가코드 역지오코딩, 실패 시 좌표 그대로 사용
 * 4. 모든 예외 → [LocaleWeatherLocationProvider] fallback
 */
class GpsWeatherLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fallback: LocaleWeatherLocationProvider,
    private val dispatchers: WalkLogDispatchers,
) : WeatherLocationProvider {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override suspend fun currentLocation(): WeatherLocation {
        if (!hasLocationPermission()) return fallback.currentLocation()

        return runCatching {
            val location = getLastKnownLocation() ?: getCurrentLocation()
                ?: return fallback.currentLocation()
            resolveWeatherLocation(location)
        }.getOrElse { fallback.currentLocation() }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        }

    @Suppress("MissingPermission")
    private suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        }

    private suspend fun resolveWeatherLocation(location: Location): WeatherLocation =
        withContext(dispatchers.io) {
            val lat = location.latitude
            val lon = location.longitude

            if (!Geocoder.isPresent()) {
                return@withContext WeatherLocation(
                    latitude = lat,
                    longitude = lon,
                    displayName = fallback.currentLocation().displayName,
                    countryCode = Locale.getDefault().country.uppercase(Locale.ROOT),
                )
            }

            geocode(lat, lon) ?: WeatherLocation(
                latitude = lat,
                longitude = lon,
                displayName = fallback.currentLocation().displayName,
                countryCode = Locale.getDefault().country.uppercase(Locale.ROOT),
            )
        }

    private suspend fun geocode(lat: Double, lon: Double): WeatherLocation? =
        suspendCancellableCoroutine { cont ->
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    cont.resume(address?.let {
                        WeatherLocation(
                            latitude = lat,
                            longitude = lon,
                            displayName = it.locality ?: it.adminArea ?: it.countryName ?: "",
                            countryCode = it.countryCode?.uppercase(Locale.ROOT) ?: "",
                        )
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val address = runCatching {
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                }.getOrNull()
                cont.resume(address?.let {
                    WeatherLocation(
                        latitude = lat,
                        longitude = lon,
                        displayName = it.locality ?: it.adminArea ?: it.countryName ?: "",
                        countryCode = it.countryCode?.uppercase(Locale.ROOT) ?: "",
                    )
                })
            }
        }
}
