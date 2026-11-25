package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppCategory
import com.example.data.AppRepository
import com.example.data.DeviceStatus
import com.example.data.DeviceStatusRepository
import com.example.data.IconStyle3D
import com.example.data.InstalledApp
import com.example.data.LauncherPreferences
import com.example.data.PageTransition3D
import com.example.data.PreferencesRepository
import com.example.data.PresetCities
import com.example.data.WallpaperPreset
import com.example.data.WeatherRepository
import com.example.data.WeatherUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val allApps: List<InstalledApp> = emptyList(),
    val visibleApps: List<InstalledApp> = emptyList(),
    val hiddenApps: List<InstalledApp> = emptyList(),
    val dockApps: List<InstalledApp> = emptyList(),
    val homePages: List<List<InstalledApp>> = listOf(emptyList()),
    val preferences: LauncherPreferences = LauncherPreferences(),
    val weatherState: WeatherUiState = WeatherUiState.Loading,
    val deviceStatus: DeviceStatus = DeviceStatus(),
    val isLoadingApps: Boolean = true,
    val isControlCenterOpen: Boolean = false,
    val isAppDrawerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val active3DApp: InstalledApp? = null,
    val drawerSearchQuery: String = "",
    val drawerSelectedCategory: AppCategory = AppCategory.ALL,
    val snackbarMessage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val preferencesRepository = PreferencesRepository(application)
    private val weatherRepository = WeatherRepository(application)
    private val deviceStatusRepository = DeviceStatusRepository(application)

    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(true)
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    private val _windowBrightness = MutableStateFlow(0.75f)

    private val _isControlCenterOpen = MutableStateFlow(false)
    private val _isAppDrawerOpen = MutableStateFlow(false)
    private val _isSettingsOpen = MutableStateFlow(false)
    private val _active3DApp = MutableStateFlow<InstalledApp?>(null)
    private val _drawerSearchQuery = MutableStateFlow("")
    private val _drawerCategory = MutableStateFlow(AppCategory.ALL)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val deviceTilt: StateFlow<Pair<Float, Float>> = deviceStatusRepository.observeDeviceTilt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f to 0f)

    private val preferencesFlow: StateFlow<LauncherPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherPreferences())

    private val baseDataFlow = combine(
        _allApps,
        _isLoadingApps,
        preferencesFlow,
        _weatherState,
        _deviceStatus
    ) { allApps, isLoading, prefs, weather, status ->
        BaseSnapshot(allApps, isLoading, prefs, weather, status)
    }

    private val overlayFlow = combine(
        _isControlCenterOpen,
        _isAppDrawerOpen,
        _isSettingsOpen,
        _active3DApp,
        _snackbarMessage
    ) { controlOpen, drawerOpen, settingsOpen, active3D, snackbar ->
        OverlaySnapshot(controlOpen, drawerOpen, settingsOpen, active3D, snackbar)
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        baseDataFlow,
        overlayFlow,
        _drawerSearchQuery,
        _drawerCategory
    ) { base, overlays, query, category ->
        val visible = base.allApps.filterNot { base.prefs.hiddenPackages.contains(it.packageName) }
        val hidden = base.allApps.filter { base.prefs.hiddenPackages.contains(it.packageName) }

        val dock = resolveDockApps(visible, base.prefs.dockPackages)
        val dockPkgSet = dock.map { it.packageName }.toSet()
        val gridPool = visible.filterNot { dockPkgSet.contains(it.packageName) }

        val cols = base.prefs.gridColumns
        val firstPageCapacity = cols * 2
        val extraPageCapacity = cols * 5

        val pages = mutableListOf<List<InstalledApp>>()
        pages.add(gridPool.take(firstPageCapacity))
        val remaining = gridPool.drop(firstPageCapacity)
        if (remaining.isNotEmpty()) {
            remaining.chunked(extraPageCapacity).forEach { chunk ->
                pages.add(chunk)
            }
        } else {
            // Always provide at least 2 smooth pages so 3D page transition can be experienced immediately
            pages.add(visible.take(cols * 3))
        }

        LauncherUiState(
            allApps = base.allApps,
            visibleApps = visible,
            hiddenApps = hidden,
            dockApps = dock,
            homePages = pages,
            preferences = base.prefs,
            weatherState = base.weather,
            deviceStatus = base.status,
            isLoadingApps = base.isLoading,
            isControlCenterOpen = overlays.controlOpen,
            isAppDrawerOpen = overlays.drawerOpen,
            isSettingsOpen = overlays.settingsOpen,
            active3DApp = overlays.active3D,
            drawerSearchQuery = query,
            drawerSelectedCategory = category,
            snackbarMessage = overlays.snackbar
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherUiState())

    val windowBrightness: StateFlow<Float> = _windowBrightness.asStateFlow()

    init {
        refreshInstalledApps()
        refreshDeviceStatus()
        observeWeatherPreferences()
        startPeriodicStatusSync()
    }

    private fun observeWeatherPreferences() {
        viewModelScope.launch {
            preferencesFlow
                .map { it.selectedCityId to it.useGpsWeather }
                .distinctUntilChanged()
                .collect { (cityId, useGps) ->
                    refreshWeather(cityId, useGps)
                }
        }
    }

    private fun startPeriodicStatusSync() {
        viewModelScope.launch {
            while (true) {
                refreshDeviceStatus()
                delay(15_000L)
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val loaded = appRepository.loadInstalledApps()
            _allApps.value = loaded
            _isLoadingApps.value = false
        }
    }

    fun refreshDeviceStatus() {
        val isDefault = appRepository.isDefaultLauncher()
        _deviceStatus.value = deviceStatusRepository.readStatus(
            isDefaultLauncher = isDefault,
            currentBrightness = _windowBrightness.value
        )
    }

    fun refreshWeather(
        cityId: String = preferencesFlow.value.selectedCityId,
        useGps: Boolean = preferencesFlow.value.useGpsWeather
    ) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            val result = weatherRepository.fetchWeather(cityId, useGps)
            _weatherState.value = result.fold(
                onSuccess = { WeatherUiState.Success(it) },
                onFailure = { err ->
                    val fallbackCityName = PresetCities.byId(cityId).name
                    WeatherUiState.Error(
                        message = err.localizedMessage ?: "Gagal memuat cuaca",
                        fallbackCity = fallbackCityName
                    )
                }
            )
        }
    }

    fun launchApp(app: InstalledApp) {
        _active3DApp.value = null
        _isAppDrawerOpen.value = false
        val launched = appRepository.launchApp(app)
        if (!launched) {
            showToast("Tidak dapat membuka ${app.label}")
        }
    }

    fun openAppInfo(app: InstalledApp) {
        _active3DApp.value = null
        appRepository.openAppInfo(app.packageName)
    }

    fun uninstallApp(app: InstalledApp) {
        _active3DApp.value = null
        if (app.isSystemApp) {
            showToast("Aplikasi sistem tidak dapat dihapus")
        } else {
            appRepository.requestUninstall(app.packageName)
        }
    }

    fun toggleDockPin(app: InstalledApp) {
        _active3DApp.value = null
        viewModelScope.launch {
            val currentDock = uiState.value.dockApps.map { it.packageName }.toMutableList()
            if (currentDock.contains(app.packageName)) {
                currentDock.remove(app.packageName)
                showToast("${app.label} dilepas dari Dock")
            } else {
                if (currentDock.size >= 4) {
                    currentDock.removeAt(currentDock.lastIndex)
                }
                currentDock.add(app.packageName)
                showToast("${app.label} disematkan ke Dock")
            }
            preferencesRepository.updateDockPackages(currentDock)
        }
    }

    fun toggleHideApp(app: InstalledApp) {
        _active3DApp.value = null
        viewModelScope.launch {
            val isHiddenNow = preferencesFlow.value.hiddenPackages.contains(app.packageName)
            preferencesRepository.toggleHiddenPackage(app.packageName)
            showToast(
                if (isHiddenNow) "${app.label} ditampilkan kembali"
                else "${app.label} disembunyikan"
            )
        }
    }

    fun setWallpaperPreset(preset: WallpaperPreset) {
        viewModelScope.launch { preferencesRepository.setWallpaperPreset(preset) }
    }

    fun setCustomWallpaperUri(uriString: String) {
        viewModelScope.launch {
            preferencesRepository.setCustomWallpaperUri(uriString)
            showToast("Wallpaper kustom diterapkan")
        }
    }

    fun setIconStyle(style: IconStyle3D) {
        viewModelScope.launch { preferencesRepository.setIconStyle(style) }
    }

    fun cycleIconStyle() {
        val styles = IconStyle3D.entries
        val next = styles[(preferencesFlow.value.iconStyle.ordinal + 1) % styles.size]
        setIconStyle(next)
        showToast("Gaya ikon: ${next.label}")
    }

    fun cycleWallpaperPreset() {
        val presets = WallpaperPreset.entries
        val next = presets[(preferencesFlow.value.wallpaperPreset.ordinal + 1) % presets.size]
        setWallpaperPreset(next)
        showToast("Wallpaper: ${next.title}")
    }

    fun setUseCustom3DGlyphs(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseCustom3DGlyphs(enabled) }
    }

    fun setPageTransition(transition: PageTransition3D) {
        viewModelScope.launch { preferencesRepository.setPageTransition(transition) }
    }

    fun setParallaxIntensity(value: Float) {
        viewModelScope.launch { preferencesRepository.setParallaxIntensity(value) }
    }

    fun setAmbientGlow(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAmbientGlow(enabled) }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { preferencesRepository.setGridColumns(columns) }
    }

    fun selectWeatherCity(cityId: String, useGps: Boolean = false) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCity(cityId, useGps)
        }
    }

    fun setControlCenterOpen(open: Boolean) {
        if (open) {
            refreshDeviceStatus()
            _isAppDrawerOpen.value = false
            _active3DApp.value = null
        }
        _isControlCenterOpen.value = open
    }

    fun setAppDrawerOpen(open: Boolean) {
        if (open) {
            _isControlCenterOpen.value = false
            _active3DApp.value = null
        } else {
            _drawerSearchQuery.value = ""
        }
        _isAppDrawerOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        if (open) {
            refreshDeviceStatus()
            _isControlCenterOpen.value = false
            _isAppDrawerOpen.value = false
            _active3DApp.value = null
        }
        _isSettingsOpen.value = open
    }

    fun setActive3DApp(app: InstalledApp?) {
        _active3DApp.value = app
    }

    fun updateDrawerSearchQuery(query: String) {
        _drawerSearchQuery.value = query
    }

    fun updateDrawerCategory(category: AppCategory) {
        _drawerCategory.value = category
    }

    fun setMediaVolume(fraction: Float) {
        deviceStatusRepository.setMediaVolume(fraction)
        _deviceStatus.value = _deviceStatus.value.copy(mediaVolumeFraction = fraction.coerceIn(0f, 1f))
    }

    fun setWindowBrightness(fraction: Float) {
        val clamped = fraction.coerceIn(0.08f, 1f)
        _windowBrightness.value = clamped
        _deviceStatus.value = _deviceStatus.value.copy(screenBrightnessFraction = clamped)
    }

    fun toggleFlashlight() {
        val next = deviceStatusRepository.toggleFlashlight()
        _deviceStatus.value = _deviceStatus.value.copy(isTorchOn = next)
    }

    fun openInternetPanel() = deviceStatusRepository.openInternetPanel()
    fun openBluetoothSettings() = deviceStatusRepository.openBluetoothSettings()
    fun openDisplaySettings() = deviceStatusRepository.openDisplaySettings()
    fun openSystemSettings() = deviceStatusRepository.openSystemSettings()
    fun openDefaultHomeSettings() = appRepository.openDefaultHomeSettings()

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    private fun showToast(msg: String) {
        _snackbarMessage.value = msg
    }

    private fun resolveDockApps(
        visibleApps: List<InstalledApp>,
        savedDockPackages: List<String>
    ): List<InstalledApp> {
        if (savedDockPackages.isEmpty()) {
            return appRepository.pickDefaultDockApps(visibleApps)
        }
        val byPkg = visibleApps.associateBy { it.packageName }
        val resolved = savedDockPackages.mapNotNull { byPkg[it] }
        return if (resolved.isNotEmpty()) resolved.take(4)
        else appRepository.pickDefaultDockApps(visibleApps)
    }

    private data class BaseSnapshot(
        val allApps: List<InstalledApp>,
        val isLoading: Boolean,
        val prefs: LauncherPreferences,
        val weather: WeatherUiState,
        val status: DeviceStatus
    )

    private data class OverlaySnapshot(
        val controlOpen: Boolean,
        val drawerOpen: Boolean,
        val settingsOpen: Boolean,
        val active3D: InstalledApp?,
        val snackbar: String?
    )
}
