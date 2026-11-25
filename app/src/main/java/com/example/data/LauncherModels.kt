package com.example.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.R

enum class IconStyle3D(val label: String, val subtitle: String) {
    GLASS_3D("3D Glass", "Efek kaca bening ala Liquid Glass"),
    CLAY_3D("3D Clay", "Tampilan lembut membulat yang taktil"),
    OBSIDIAN_3D("Obsidian 3D", "Minimalis gelap elegan untuk AMOLED")
}

enum class PageTransition3D(val label: String, val subtitle: String) {
    CUBE_DEPTH("3D Cube Depth", "Perspektif kubus dalam yang halus"),
    CAROUSEL_ARC("3D Carousel", "Lengkung spasial dengan kedalaman"),
    GLASS_FLIP("3D Glass Flip", "Putaran kartu 3D responsif")
}

enum class WallpaperPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val drawableRes: Int,
    val isLightBackdrop: Boolean,
    val accentColor: Color
) {
    OBSIDIAN_SILK(
        id = "obsidian_silk",
        title = "Obsidian Silk",
        subtitle = "Sutra kaca biru elektrik untuk AMOLED",
        drawableRes = R.drawable.img_wall_obsidian_silk_1795598625755,
        isLightBackdrop = false,
        accentColor = Color(0xFF5CE1FF)
    ),
    AURORA_MIST(
        id = "aurora_mist",
        title = "Aurora Mist",
        subtitle = "Cahaya aurora tenang bernuansa malam",
        drawableRes = R.drawable.img_wall_aurora_mist_1795598627710,
        isLightBackdrop = false,
        accentColor = Color(0xFF4BE3B5)
    ),
    TITANIUM_PEARL(
        id = "titanium_pearl",
        title = "Titanium Pearl",
        subtitle = "Minimalis terang elegan bergaya iPhone Pro",
        drawableRes = R.drawable.img_wall_titanium_sand_1795598627618,
        isLightBackdrop = true,
        accentColor = Color(0xFFD6B98C)
    );

    companion object {
        fun fromId(id: String?): WallpaperPreset =
            entries.firstOrNull { it.id == id } ?: OBSIDIAN_SILK
    }
}

enum class AppCategory(val label: String) {
    ALL("Semua"),
    AI("AI"),
    GAMES("Game"),
    ESSENTIALS("Penting"),
    MEDIA("Media"),
    UTILITIES("Utilitas")
}

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconBitmap: ImageBitmap?,
    val category: AppCategory,
    val isSystemApp: Boolean
)

data class LauncherPreferences(
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.OBSIDIAN_SILK,
    val customWallpaperUri: String? = null,
    val iconStyle: IconStyle3D = IconStyle3D.GLASS_3D,
    val useCustom3DGlyphs: Boolean = true,
    val pageTransition: PageTransition3D = PageTransition3D.CUBE_DEPTH,
    val parallaxIntensity: Float = 0.78f,
    val ambientGlowEnabled: Boolean = true,
    val immersiveGestureNav: Boolean = true,
    val gridColumns: Int = 4,
    val dockPackages: List<String> = emptyList(),
    val hiddenPackages: Set<String> = emptySet(),
    val selectedCityId: String = "jakarta",
    val useGpsWeather: Boolean = false
)

enum class WeatherScene {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    CLOUDY,
    RAIN,
    STORM,
    FOG
}

data class HourlyForecastItem(
    val hourLabel: String,
    val temperatureC: Int,
    val weatherCode: Int,
    val scene: WeatherScene
)

data class DailyForecastItem(
    val dayLabel: String,
    val maxTempC: Int,
    val minTempC: Int,
    val weatherCode: Int,
    val scene: WeatherScene,
    val rainChancePercent: Int
)

data class WeatherData(
    val cityName: String,
    val temperatureC: Int,
    val feelsLikeC: Int,
    val conditionLabel: String,
    val scene: WeatherScene,
    val isDay: Boolean,
    val humidityPercent: Int,
    val windKmh: Int,
    val highTempC: Int,
    val lowTempC: Int,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val updatedAtLabel: String
)

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String, val fallbackCity: String) : WeatherUiState
}

data class CityLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object PresetCities {
    val all = listOf(
        CityLocation("jakarta", "Jakarta", -6.2088, 106.8456),
        CityLocation("bandung", "Bandung", -6.9175, 107.6191),
        CityLocation("surabaya", "Surabaya", -7.2575, 112.7521),
        CityLocation("yogyakarta", "Yogyakarta", -7.7956, 110.3695),
        CityLocation("denpasar", "Bali", -8.6705, 115.2126),
        CityLocation("medan", "Medan", 3.5952, 98.6722),
        CityLocation("makassar", "Makassar", -5.1477, 119.4327),
        CityLocation("semarang", "Semarang", -6.9667, 110.4167),
        CityLocation("singapore", "Singapura", 1.3521, 103.8198),
        CityLocation("tokyo", "Tokyo", 35.6762, 139.6503)
    )

    fun byId(id: String): CityLocation = all.firstOrNull { it.id == id } ?: all.first()
}

enum class TopShadeMode {
    CLOSED,
    CONTROL_CENTER,
    NOTIFICATIONS
}

data class DeviceStatus(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isWifiConnected: Boolean = true,
    val isCellularConnected: Boolean = false,
    val isAirplaneModeOn: Boolean = false,
    val mediaVolumeFraction: Float = 0.6f,
    val screenBrightnessFraction: Float = 0.72f,
    val isTorchOn: Boolean = false,
    val isDefaultLauncher: Boolean = false,
    val availableRamGb: Float = 6.4f,
    val totalRamGb: Float = 12.0f
)
