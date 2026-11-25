package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherRepository(private val context: Context) {

    private val apiService: WeatherApiService by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    suspend fun fetchWeather(cityId: String, useGps: Boolean): Result<WeatherData> = withContext(Dispatchers.IO) {
        runCatching {
            val fallbackCity = PresetCities.byId(cityId)
            val resolvedCoords = if (useGps) {
                resolveCoarseLocation() ?: Triple(fallbackCity.latitude, fallbackCity.longitude, fallbackCity.name)
            } else {
                Triple(fallbackCity.latitude, fallbackCity.longitude, fallbackCity.name)
            }

            val (lat, lon, cityLabel) = resolvedCoords
            val response = apiService.getForecast(latitude = lat, longitude = lon)
            mapResponseToWeatherData(response, cityLabel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun resolveCoarseLocation(): Triple<Double, Double, String>? {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarse) return null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        var best: Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            if (loc != null && (best == null || loc.time > best.time)) {
                best = loc
            }
        }
        val location = best ?: return null
        val cityName = runCatching {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale("id", "ID"))
                .getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { addr ->
                addr.subAdminArea ?: addr.locality ?: addr.adminArea
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Lokasi Saat Ini"

        return Triple(location.latitude, location.longitude, cityName)
    }

    private fun mapResponseToWeatherData(
        dto: OpenMeteoResponse,
        cityName: String
    ): WeatherData {
        val current = dto.current
        val isDay = (current?.isDay ?: 1) == 1
        val code = current?.weatherCode ?: 0
        val temp = current?.temperature2m?.roundToInt() ?: 28
        val feelsLike = current?.apparentTemperature?.roundToInt() ?: temp
        val humidity = current?.relativeHumidity2m ?: 70
        val wind = current?.windSpeed10m?.roundToInt() ?: 8

        val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourlyTimes = dto.hourly?.time.orEmpty()
        val hourlyTemps = dto.hourly?.temperature2m.orEmpty()
        val hourlyCodes = dto.hourly?.weatherCode.orEmpty()

        val startIdx = hourlyTimes.indexOfFirst { timeStr ->
            val hourPart = timeStr.substringAfter("T", "00").take(2).toIntOrNull() ?: 0
            hourPart >= nowHour
        }.coerceAtLeast(0)

        val hourlyList = mutableListOf<HourlyForecastItem>()
        for (offset in 0 until 8) {
            val idx = startIdx + offset
            if (idx < hourlyTimes.size && idx < hourlyTemps.size) {
                val timeRaw = hourlyTimes[idx].substringAfter("T", "00:00").take(5)
                val hourNum = timeRaw.take(2).toIntOrNull() ?: 12
                val itemIsDay = hourNum in 6..18
                val hCode = hourlyCodes.getOrElse(idx) { code }
                hourlyList.add(
                    HourlyForecastItem(
                        hourLabel = if (offset == 0) "Skrg" else timeRaw,
                        temperatureC = hourlyTemps[idx].roundToInt(),
                        weatherCode = hCode,
                        scene = mapCodeToScene(hCode, itemIsDay)
                    )
                )
            }
        }

        val dailyTimes = dto.daily?.time.orEmpty()
        val dailyMax = dto.daily?.temperature2mMax.orEmpty()
        val dailyMin = dto.daily?.temperature2mMin.orEmpty()
        val dailyCodes = dto.daily?.weatherCode.orEmpty()
        val dailyRain = dto.daily?.precipitationProbabilityMax.orEmpty()

        val dailyList = dailyTimes.indices.take(5).map { idx ->
            val dCode = dailyCodes.getOrElse(idx) { code }
            DailyForecastItem(
                dayLabel = formatDayLabel(dailyTimes[idx], idx == 0),
                maxTempC = dailyMax.getOrElse(idx) { temp.toDouble() + 2 }.roundToInt(),
                minTempC = dailyMin.getOrElse(idx) { temp.toDouble() - 3 }.roundToInt(),
                weatherCode = dCode,
                scene = mapCodeToScene(dCode, true),
                rainChancePercent = dailyRain.getOrElse(idx) { 10 }
            )
        }

        val todayHigh = dailyList.firstOrNull()?.maxTempC ?: (temp + 2)
        val todayLow = dailyList.firstOrNull()?.minTempC ?: (temp - 3)
        val updatedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        return WeatherData(
            cityName = cityName,
            temperatureC = temp,
            feelsLikeC = feelsLike,
            conditionLabel = mapCodeToIndonesianLabel(code, isDay),
            scene = mapCodeToScene(code, isDay),
            isDay = isDay,
            humidityPercent = humidity,
            windKmh = wind,
            highTempC = todayHigh,
            lowTempC = todayLow,
            hourly = hourlyList,
            daily = dailyList,
            updatedAtLabel = updatedTime
        )
    }

    private fun formatDayLabel(isoDate: String, isToday: Boolean): String {
        if (isToday) return "Hari Ini"
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return isoDate
            val cal = Calendar.getInstance().apply { time = date }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Min"
                Calendar.MONDAY -> "Sen"
                Calendar.TUESDAY -> "Sel"
                Calendar.WEDNESDAY -> "Rab"
                Calendar.THURSDAY -> "Kam"
                Calendar.FRIDAY -> "Jum"
                Calendar.SATURDAY -> "Sab"
                else -> isoDate.takeLast(2)
            }
        }.getOrDefault(isoDate.takeLast(2))
    }

    private fun mapCodeToScene(code: Int, isDay: Boolean): WeatherScene {
        return when (code) {
            0, 1 -> if (isDay) WeatherScene.CLEAR_DAY else WeatherScene.CLEAR_NIGHT
            2 -> WeatherScene.PARTLY_CLOUDY
            3 -> WeatherScene.CLOUDY
            45, 48 -> WeatherScene.FOG
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherScene.RAIN
            95, 96, 99 -> WeatherScene.STORM
            else -> if (isDay) WeatherScene.PARTLY_CLOUDY else WeatherScene.CLEAR_NIGHT
        }
    }

    private fun mapCodeToIndonesianLabel(code: Int, isDay: Boolean): String {
        return when (code) {
            0 -> if (isDay) "Cerah" else "Malam Cerah"
            1 -> "Cerah Berawan"
            2 -> "Berawan Sebagian"
            3 -> "Mendung"
            45, 48 -> "Berkabut"
            51, 53, 55 -> "Gerimis Halus"
            61, 63 -> "Hujan Ringan"
            65, 80, 81, 82 -> "Hujan Deras"
            95, 96, 99 -> "Badai Petir"
            else -> "Berawan"
        }
    }
}
