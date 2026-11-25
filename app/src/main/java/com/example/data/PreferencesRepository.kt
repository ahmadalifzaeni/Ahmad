package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lumina_f7_preferences"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val ICON_STYLE = stringPreferencesKey("icon_style")
        val USE_CUSTOM_3D_GLYPHS = booleanPreferencesKey("use_custom_3d_glyphs")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")
        val PARALLAX_INTENSITY = floatPreferencesKey("parallax_intensity")
        val AMBIENT_GLOW = booleanPreferencesKey("ambient_glow")
        val IMMERSIVE_GESTURE_NAV = booleanPreferencesKey("immersive_gesture_nav")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val DOCK_PACKAGES = stringPreferencesKey("dock_packages_csv")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
        val SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
        val USE_GPS_WEATHER = booleanPreferencesKey("use_gps_weather")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.launcherDataStore.data.map { prefs ->
        val dockCsv = prefs[Keys.DOCK_PACKAGES].orEmpty()
        val dockList = if (dockCsv.isBlank()) emptyList() else dockCsv.split("|").filter { it.isNotBlank() }

        LauncherPreferences(
            wallpaperPreset = WallpaperPreset.fromId(prefs[Keys.WALLPAPER_PRESET]),
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI]?.takeIf { it.isNotBlank() },
            iconStyle = prefs[Keys.ICON_STYLE]?.let { name ->
                IconStyle3D.entries.firstOrNull { it.name == name }
            } ?: IconStyle3D.GLASS_3D,
            useCustom3DGlyphs = prefs[Keys.USE_CUSTOM_3D_GLYPHS] ?: true,
            pageTransition = prefs[Keys.PAGE_TRANSITION]?.let { name ->
                PageTransition3D.entries.firstOrNull { it.name == name }
            } ?: PageTransition3D.CUBE_DEPTH,
            parallaxIntensity = prefs[Keys.PARALLAX_INTENSITY] ?: 0.78f,
            ambientGlowEnabled = prefs[Keys.AMBIENT_GLOW] ?: true,
            immersiveGestureNav = prefs[Keys.IMMERSIVE_GESTURE_NAV] ?: true,
            gridColumns = (prefs[Keys.GRID_COLUMNS] ?: 4).coerceIn(4, 5),
            dockPackages = dockList,
            hiddenPackages = prefs[Keys.HIDDEN_PACKAGES] ?: emptySet(),
            selectedCityId = prefs[Keys.SELECTED_CITY_ID] ?: "jakarta",
            useGpsWeather = prefs[Keys.USE_GPS_WEATHER] ?: false
        )
    }

    suspend fun setWallpaperPreset(preset: WallpaperPreset) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_PRESET] = preset.id
            prefs.remove(Keys.CUSTOM_WALLPAPER_URI)
        }
    }

    suspend fun setCustomWallpaperUri(uri: String?) {
        context.launcherDataStore.edit { prefs ->
            if (uri.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_WALLPAPER_URI)
            } else {
                prefs[Keys.CUSTOM_WALLPAPER_URI] = uri
            }
        }
    }

    suspend fun setIconStyle(style: IconStyle3D) {
        context.launcherDataStore.edit { it[Keys.ICON_STYLE] = style.name }
    }

    suspend fun setUseCustom3DGlyphs(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.USE_CUSTOM_3D_GLYPHS] = enabled }
    }

    suspend fun setPageTransition(transition: PageTransition3D) {
        context.launcherDataStore.edit { it[Keys.PAGE_TRANSITION] = transition.name }
    }

    suspend fun setParallaxIntensity(value: Float) {
        context.launcherDataStore.edit { it[Keys.PARALLAX_INTENSITY] = value.coerceIn(0f, 1f) }
    }

    suspend fun setAmbientGlow(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.AMBIENT_GLOW] = enabled }
    }

    suspend fun setImmersiveGestureNav(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.IMMERSIVE_GESTURE_NAV] = enabled }
    }

    suspend fun setGridColumns(columns: Int) {
        context.launcherDataStore.edit { it[Keys.GRID_COLUMNS] = columns.coerceIn(4, 5) }
    }

    suspend fun updateDockPackages(packages: List<String>) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.DOCK_PACKAGES] = packages.distinct().take(4).joinToString("|")
        }
    }

    suspend fun toggleHiddenPackage(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.HIDDEN_PACKAGES] = current
        }
    }

    suspend fun setSelectedCity(cityId: String, useGps: Boolean) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.SELECTED_CITY_ID] = cityId
            prefs[Keys.USE_GPS_WEATHER] = useGps
        }
    }
}
