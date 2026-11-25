package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AppCategory
import com.example.data.DeviceStatus
import com.example.data.IconStyle3D
import com.example.data.InstalledApp
import com.example.data.PageTransition3D
import com.example.data.TopShadeMode
import com.example.data.WeatherUiState
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppIcon3DItem
import com.example.ui.components.DynamicIslandTopBar
import com.example.ui.components.GestureNavigationPill
import com.example.ui.components.Interactive3DAppOverlay
import com.example.ui.components.InteractiveWeatherWidget
import com.example.ui.components.LauncherSettingsSheet
import com.example.ui.components.LiquidGlassFolderPopup
import com.example.ui.components.ParallaxWallpaperLayer
import com.example.ui.components.ProMaxBentoWidgetRow
import com.example.ui.components.ProMaxHeroClockHeader
import com.example.ui.components.SmartCuratedFoldersRow
import com.example.ui.components.TopControlCenterOverlay
import com.example.ui.theme.LuminaCyan
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tilt by viewModel.deviceTilt.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var timeLabel by remember { mutableStateOf(formatCurrentTime()) }
    var dateLabel by remember { mutableStateOf(formatCurrentDate()) }
    var dayOfWeekLabel by remember { mutableStateOf(formatDayOfWeek()) }
    var dayOfMonthLabel by remember { mutableStateOf(formatDayOfMonth()) }
    var monthYearLabel by remember { mutableStateOf(formatMonthYear()) }

    LaunchedEffect(Unit) {
        while (true) {
            timeLabel = formatCurrentTime()
            dateLabel = formatCurrentDate()
            dayOfWeekLabel = formatDayOfWeek()
            dayOfMonthLabel = formatDayOfMonth()
            monthYearLabel = formatMonthYear()
            delay(15_000L)
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.selectWeatherCity(state.preferences.selectedCityId, useGps = true)
        } else {
            viewModel.refreshWeather(state.preferences.selectedCityId, useGps = false)
        }
    }

    val pageCount = 2
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    val pagerScrollFraction = remember(
        pagerState.currentPage,
        pagerState.currentPageOffsetFraction
    ) {
        ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pageCount - 1).toFloat())
            .coerceIn(0f, 1f)
    }

    val isOverlayOpen = state.topShadeMode != TopShadeMode.CLOSED ||
        state.isAppDrawerOpen ||
        state.isSettingsOpen ||
        state.activeFolderCategory != null ||
        state.active3DApp != null

    val homeBlurRadius by animateDpAsState(
        targetValue = if (isOverlayOpen) 26.dp else 0.dp,
        animationSpec = tween(durationMillis = 240),
        label = "full_home_blur_dp"
    )
    val homeDepthScale by animateFloatAsState(
        targetValue = if (isOverlayOpen) 0.94f else 1f,
        animationSpec = spring(),
        label = "full_home_depth_scale"
    )

    val density = LocalDensity.current.density

    val weatherIslandSummary = remember(state.weatherState) {
        (state.weatherState as? WeatherUiState.Success)?.data?.let {
            "${it.temperatureC}°  •  ${it.conditionLabel}"
        }
    }

    val activeFolderApps = remember(
        state.activeFolderCategory,
        state.aiApps,
        state.gameApps,
        state.essentialApps,
        state.mediaApps,
        state.utilityApps
    ) {
        when (state.activeFolderCategory) {
            AppCategory.AI -> state.aiApps
            AppCategory.GAMES -> state.gameApps
            AppCategory.ESSENTIALS -> state.essentialApps
            AppCategory.MEDIA -> state.mediaApps
            AppCategory.UTILITIES -> state.utilityApps
            else -> state.visibleApps
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isOverlayOpen) {
                if (!isOverlayOpen) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount > 34f) {
                            if (change.position.x < size.width * 0.45f) {
                                viewModel.setTopShadeMode(TopShadeMode.NOTIFICATIONS)
                            } else {
                                viewModel.setTopShadeMode(TopShadeMode.CONTROL_CENTER)
                            }
                        } else if (dragAmount < -34f) {
                            viewModel.setAppDrawerOpen(true)
                        }
                    }
                }
            }
    ) {
        // ENTIRE Home Layer (Wallpaper + Top Dynamic Island + Clock + Widgets + Icons + Dock)
        // blurs smoothly as ONE unit when Control Center, Notifications, or any overlay opens!
        val fullBlurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && homeBlurRadius > 0.5.dp) {
            Modifier.blur(homeBlurRadius)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(fullBlurModifier)
                .graphicsLayer {
                    scaleX = homeDepthScale
                    scaleY = homeDepthScale
                }
        ) {
            // 1. High-resolution dual-axis smooth Parallax Wallpaper Layer
            ParallaxWallpaperLayer(
                preset = state.preferences.wallpaperPreset,
                customWallpaperUri = state.preferences.customWallpaperUri,
                pagerScrollFraction = pagerScrollFraction,
                tiltX = tilt.first,
                tiltY = tilt.second,
                parallaxIntensity = state.preferences.parallaxIntensity,
                ambientGlowEnabled = state.preferences.ambientGlowEnabled,
                isBackdropBlurred = false
            )

            // 2. Main Edge-to-Edge iPhone 18 Pro Max Home Foreground
            val bottomPaddingModifier = if (state.preferences.immersiveGestureNav) {
                Modifier.padding(bottom = 6.dp)
            } else {
                Modifier.navigationBarsPadding()
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .then(bottomPaddingModifier)
            ) {
                // Single unified centered Dynamic Island (no duplicate status bar!)
                DynamicIslandTopBar(
                    weatherSummary = weatherIslandSummary,
                    deviceStatus = state.deviceStatus,
                    onOpenControlCenter = { viewModel.setTopShadeMode(TopShadeMode.CONTROL_CENTER) },
                    onOpenNotifications = { viewModel.setTopShadeMode(TopShadeMode.NOTIFICATIONS) },
                    onOpenSettings = { viewModel.setSettingsOpen(true) }
                )

                // Interactive 3D Pager
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .weight(1f)
                        .testTag("home_3d_pager")
                ) { pageIndex ->
                    val pageOffset =
                        (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density
                                when (state.preferences.pageTransition) {
                                    PageTransition3D.CUBE_DEPTH -> {
                                        rotationY = (-pageOffset * 34f).coerceIn(-42f, 42f)
                                        transformOrigin = if (pageOffset >= 0f) {
                                            TransformOrigin(1f, 0.5f)
                                        } else {
                                            TransformOrigin(0f, 0.5f)
                                        }
                                        val s = 1f - (absOffset * 0.10f)
                                        scaleX = s
                                        scaleY = s
                                        alpha = 1f - (absOffset * 0.25f)
                                    }

                                    PageTransition3D.CAROUSEL_ARC -> {
                                        rotationY = (pageOffset * 26f).coerceIn(-32f, 32f)
                                        rotationZ = (-pageOffset * 3.5f)
                                        val s = 1f - (absOffset * 0.14f)
                                        scaleX = s
                                        scaleY = s
                                        translationY = absOffset * 36f
                                        alpha = 1f - (absOffset * 0.30f)
                                    }

                                    PageTransition3D.GLASS_FLIP -> {
                                        rotationY = (pageOffset * 52f).coerceIn(-60f, 60f)
                                        val s = 1f - (absOffset * 0.16f)
                                        scaleX = s
                                        scaleY = s
                                        alpha = (1f - absOffset * 0.55f).coerceIn(0.35f, 1f)
                                    }
                                }
                            }
                            .padding(horizontal = 18.dp)
                    ) {
                        if (pageIndex == 0) {
                            ProMaxMinimalHomePage(
                                timeLabel = timeLabel,
                                dateLabel = dateLabel,
                                dayOfWeekLabel = dayOfWeekLabel,
                                dayOfMonthLabel = dayOfMonthLabel,
                                monthYearLabel = monthYearLabel,
                                deviceStatus = state.deviceStatus,
                                weatherState = state.weatherState,
                                selectedCityId = state.preferences.selectedCityId,
                                useGpsWeather = state.preferences.useGpsWeather,
                                aiApps = state.aiApps,
                                gameApps = state.gameApps,
                                essentialApps = state.essentialApps,
                                featuredHomeApps = state.featuredHomeApps,
                                iconStyle = state.preferences.iconStyle,
                                useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                                tiltX = tilt.first,
                                tiltY = tilt.second,
                                onClockClick = { viewModel.openClockApp() },
                                onBatteryCardClick = { viewModel.setTopShadeMode(TopShadeMode.CONTROL_CENTER) },
                                onCalendarCardClick = { viewModel.openCalendarApp() },
                                onRefreshWeather = { viewModel.refreshWeather() },
                                onSelectCity = { viewModel.selectWeatherCity(it, useGps = false) },
                                onRequestGps = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                },
                                onOpenFolder = { viewModel.setActiveFolderCategory(it) },
                                onLaunchApp = { viewModel.launchApp(it) },
                                onLongPressApp = { viewModel.setActive3DApp(it) }
                            )
                        } else {
                            SmartAppLibraryPage(
                                aiApps = state.aiApps,
                                gameApps = state.gameApps,
                                essentialApps = state.essentialApps,
                                mediaApps = state.mediaApps,
                                utilityApps = state.utilityApps,
                                iconStyle = state.preferences.iconStyle,
                                useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                                tiltX = tilt.first,
                                tiltY = tilt.second,
                                onOpenSearchDrawer = { viewModel.setAppDrawerOpen(true) },
                                onOpenFolder = { viewModel.setActiveFolderCategory(it) },
                                onLaunchApp = { viewModel.launchApp(it) },
                                onLongPressApp = { viewModel.setActive3DApp(it) }
                            )
                        }
                    }
                }

                // Minimalist iPhone Spotlight Search Pill + Page Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(0.7.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.setAppDrawerOpen(true) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("spotlight_search_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.cd_search_apps),
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Cari",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.90f)
                        )
                    }

                    repeat(pageCount) { idx ->
                        val isSelected = pagerState.currentPage == idx
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else 7.dp,
                            animationSpec = spring(),
                            label = "pager_dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) LuminaCyan else Color.White.copy(alpha = 0.36f)
                                )
                        )
                    }
                }

                // Floating iPhone 18 Pro Max Liquid-Glass 4-Icon Dock
                FloatingGlassDock(
                    dockApps = state.dockApps,
                    iconStyle = state.preferences.iconStyle,
                    useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                    tiltX = tilt.first,
                    tiltY = tilt.second,
                    onLaunchApp = { viewModel.launchApp(it) },
                    onLongPressApp = { viewModel.setActive3DApp(it) },
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 4.dp)
                        .widthIn(max = 520.dp)
                )

                // Interactive Gesture Navigation Pill (Replaces 3-Button Bar)
                GestureNavigationPill(
                    onTapHome = {
                        if (isOverlayOpen) {
                            viewModel.closeAllOverlays()
                        } else if (pagerState.currentPage != 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    },
                    onSwipeUp = {
                        if (isOverlayOpen) {
                            viewModel.closeAllOverlays()
                        } else {
                            viewModel.setAppDrawerOpen(true)
                        }
                    },
                    onSwipeLeft = {
                        val next = (pagerState.currentPage + 1).coerceAtMost(pageCount - 1)
                        coroutineScope.launch { pagerState.animateScrollToPage(next) }
                    },
                    onSwipeRight = {
                        val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                        coroutineScope.launch { pagerState.animateScrollToPage(prev) }
                    },
                    onLongPressSearch = {
                        viewModel.setAppDrawerOpen(true)
                    }
                )
            }
        }

        // 3. Full-Screen Blurred Control Center & Notification Center Shade
        TopControlCenterOverlay(
            mode = state.topShadeMode,
            timeLabel = timeLabel,
            dateLabel = dateLabel,
            deviceStatus = state.deviceStatus,
            weatherState = state.weatherState,
            wallpaperPreset = state.preferences.wallpaperPreset,
            iconStyle = state.preferences.iconStyle,
            onSwitchMode = { viewModel.setTopShadeMode(it) },
            onDismiss = { viewModel.setTopShadeMode(TopShadeMode.CLOSED) },
            onBrightnessChange = { viewModel.setWindowBrightness(it) },
            onVolumeChange = { viewModel.setMediaVolume(it) },
            onToggleTorch = { viewModel.toggleFlashlight() },
            onCycleWallpaper = { viewModel.cycleWallpaperPreset() },
            onCycleIconStyle = { viewModel.cycleIconStyle() },
            onOpenInternet = { viewModel.openInternetPanel() },
            onOpenAirplaneMode = { viewModel.openAirplaneModeSettings() },
            onOpenBluetooth = { viewModel.openBluetoothSettings() },
            onOpenNotificationSettings = { viewModel.openNotificationSettings() },
            onOpenSystemSettings = { viewModel.openSystemSettings() },
            onOpenLauncherSettings = { viewModel.setSettingsOpen(true) },
            onOpenDefaultHome = { viewModel.openDefaultHomeSettings() }
        )

        // 4. Liquid Glass Smart Folder Popup (AI / Game / Penting)
        LiquidGlassFolderPopup(
            category = state.activeFolderCategory,
            apps = activeFolderApps,
            iconStyle = state.preferences.iconStyle,
            useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
            onDismiss = { viewModel.setActiveFolderCategory(null) },
            onLaunchApp = { viewModel.launchApp(it) },
            onLongPressApp = { viewModel.setActive3DApp(it) },
            onOpenAllDrawer = { viewModel.setAppDrawerOpen(true) }
        )

        // 5. Full App Drawer / Spotlight Search Sheet
        AppDrawerSheet(
            visible = state.isAppDrawerOpen,
            apps = state.visibleApps,
            searchQuery = state.drawerSearchQuery,
            selectedCategory = state.drawerSelectedCategory,
            gridColumns = state.preferences.gridColumns,
            iconStyle = state.preferences.iconStyle,
            useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
            onSearchQueryChange = { viewModel.updateDrawerSearchQuery(it) },
            onCategoryChange = { viewModel.updateDrawerCategory(it) },
            onLaunchApp = { viewModel.launchApp(it) },
            onLongPressApp = { viewModel.setActive3DApp(it) },
            onDismiss = { viewModel.setAppDrawerOpen(false) }
        )

        // 6. Full Launcher Customization Sheet (with pinned transparent top bar)
        LauncherSettingsSheet(
            visible = state.isSettingsOpen,
            preferences = state.preferences,
            isDefaultLauncher = state.deviceStatus.isDefaultLauncher,
            hiddenApps = state.hiddenApps,
            onDismiss = { viewModel.setSettingsOpen(false) },
            onOpenDefaultHome = { viewModel.openDefaultHomeSettings() },
            onSelectWallpaperPreset = { viewModel.setWallpaperPreset(it) },
            onPickCustomWallpaper = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onParallaxIntensityChange = { viewModel.setParallaxIntensity(it) },
            onAmbientGlowChange = { viewModel.setAmbientGlow(it) },
            onImmersiveGestureChange = { viewModel.setImmersiveGestureNav(it) },
            onOpenGestureSystemSettings = { viewModel.openSystemGestureSettings() },
            onSelectIconStyle = { viewModel.setIconStyle(it) },
            onToggleCustom3DGlyphs = { viewModel.setUseCustom3DGlyphs(it) },
            onSelectPageTransition = { viewModel.setPageTransition(it) },
            onSelectGridColumns = { viewModel.setGridColumns(it) },
            onUnhideApp = { viewModel.toggleHideApp(it) }
        )

        // 7. Interactive 3D Hold App Overlay ("menyentuh aplikasi saat ditahan")
        Interactive3DAppOverlay(
            app = state.active3DApp,
            isPinnedToDock = state.active3DApp?.let { held ->
                state.dockApps.any { it.packageName == held.packageName }
            } ?: false,
            iconStyle = state.preferences.iconStyle,
            useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
            deviceTiltX = tilt.first,
            deviceTiltY = tilt.second,
            onDismiss = { viewModel.setActive3DApp(null) },
            onOpen = { viewModel.launchApp(it) },
            onToggleDock = { viewModel.toggleDockPin(it) },
            onAppInfo = { viewModel.openAppInfo(it) },
            onHideApp = { viewModel.toggleHideApp(it) },
            onUninstall = { viewModel.uninstallApp(it) }
        )

        // Minimalist Floating Glass Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 116.dp, start = 20.dp, end = 20.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xE6141C2E),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

@Composable
private fun ProMaxMinimalHomePage(
    timeLabel: String,
    dateLabel: String,
    dayOfWeekLabel: String,
    dayOfMonthLabel: String,
    monthYearLabel: String,
    deviceStatus: DeviceStatus,
    weatherState: WeatherUiState,
    selectedCityId: String,
    useGpsWeather: Boolean,
    aiApps: List<InstalledApp>,
    gameApps: List<InstalledApp>,
    essentialApps: List<InstalledApp>,
    featuredHomeApps: List<InstalledApp>,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onClockClick: () -> Unit,
    onBatteryCardClick: () -> Unit,
    onCalendarCardClick: () -> Unit,
    onRefreshWeather: () -> Unit,
    onSelectCity: (String) -> Unit,
    onRequestGps: () -> Unit,
    onOpenFolder: (AppCategory) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 6.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. iPhone 18 Pro Max Editorial Hero Clock & Date
        ProMaxHeroClockHeader(
            timeLabel = timeLabel,
            dateLabel = dateLabel,
            onClockClick = onClockClick
        )

        // 2. Integrated Interactive Live Weather Liquid-Glass Widget
        InteractiveWeatherWidget(
            weatherState = weatherState,
            selectedCityId = selectedCityId,
            useGpsWeather = useGpsWeather,
            onRefresh = onRefreshWeather,
            onSelectCity = onSelectCity,
            onRequestGpsLocation = onRequestGps
        )

        // 3. Bento Mini Widgets (Battery/RAM Ring + Calendar)
        ProMaxBentoWidgetRow(
            deviceStatus = deviceStatus,
            dayOfWeekLabel = dayOfWeekLabel,
            dayOfMonthLabel = dayOfMonthLabel,
            monthYearLabel = monthYearLabel,
            onBatteryCardClick = onBatteryCardClick,
            onCalendarCardClick = onCalendarCardClick
        )

        // 4. Clean Curated Smart Folders: AI, Game, Penting (Zero clutter!)
        SmartCuratedFoldersRow(
            aiApps = aiApps,
            gameApps = gameApps,
            essentialApps = essentialApps,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            onOpenFolder = onOpenFolder
        )

        // 5. Single Minimalist Row of Top 4 Curated Essential/AI/Game Apps
        if (featuredHomeApps.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                featuredHomeApps.take(4).forEach { app ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        AppIcon3DItem(
                            app = app,
                            iconStyle = iconStyle,
                            useCustom3DGlyphs = useCustom3DGlyphs,
                            showLabel = true,
                            iconSize = 56.dp,
                            tiltX = tiltX,
                            tiltY = tiltY,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onLongPressApp(app) }
                        )
                    }
                }
                repeat((4 - featuredHomeApps.size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SmartAppLibraryPage(
    aiApps: List<InstalledApp>,
    gameApps: List<InstalledApp>,
    essentialApps: List<InstalledApp>,
    mediaApps: List<InstalledApp>,
    utilityApps: List<InstalledApp>,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onOpenSearchDrawer: () -> Unit,
    onOpenFolder: (AppCategory) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 12.dp)
    ) {
        // iOS-style App Library Search Bar
        val searchShape = RoundedCornerShape(22.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(searchShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(0.8.dp, Color.White.copy(alpha = 0.28f), searchShape)
                .clickable(onClick = onOpenSearchDrawer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("library_search_bar")
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.cd_search_apps),
                tint = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Pustaka Aplikasi  •  Cari semua aplikasi...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )
        }

        // Categorized Clean Sections (AI, Game, Penting, Media)
        val sections = listOf(
            Triple("AI & Asisten", AppCategory.AI, aiApps),
            Triple("Game", AppCategory.GAMES, gameApps),
            Triple("Penting", AppCategory.ESSENTIALS, essentialApps),
            Triple("Media & Sosial", AppCategory.MEDIA, mediaApps),
            Triple("Utilitas", AppCategory.UTILITIES, utilityApps)
        ).filter { it.third.isNotEmpty() }

        sections.forEach { (title, category, apps) ->
            val cardShape = RoundedCornerShape(26.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.22f), cardShape)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (apps.size > 4) {
                        Text(
                            text = "Lihat semua (${apps.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = LuminaCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenFolder(category) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    apps.take(4).forEach { app ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIcon3DItem(
                                app = app,
                                iconStyle = iconStyle,
                                useCustom3DGlyphs = useCustom3DGlyphs,
                                showLabel = true,
                                iconSize = 52.dp,
                                tiltX = tiltX,
                                tiltY = tiltY,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onLongPressApp(app) }
                            )
                        }
                    }
                    repeat((4 - apps.take(4).size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingGlassDock(
    dockApps: List<InstalledApp>,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(32.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .clip(dockShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.White.copy(alpha = 0.11f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.14f)
                    )
                ),
                shape = dockShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("floating_glass_dock")
    ) {
        dockApps.take(4).forEach { app ->
            AppIcon3DItem(
                app = app,
                iconStyle = iconStyle,
                useCustom3DGlyphs = useCustom3DGlyphs,
                showLabel = false,
                iconSize = 56.dp,
                tiltX = tiltX,
                tiltY = tiltY,
                onClick = { onLaunchApp(app) },
                onLongClick = { onLongPressApp(app) }
            )
        }
    }
}

private fun formatCurrentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun formatCurrentDate(): String =
    SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())

private fun formatDayOfWeek(): String =
    SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())

private fun formatDayOfMonth(): String =
    SimpleDateFormat("d", Locale.getDefault()).format(Date())

private fun formatMonthYear(): String =
    SimpleDateFormat("MMMM", Locale("id", "ID")).format(Date())
