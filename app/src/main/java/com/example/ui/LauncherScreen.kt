package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.rounded.Apps
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.data.InstalledApp
import com.example.data.PageTransition3D
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppIcon3DItem
import com.example.ui.components.Interactive3DAppOverlay
import com.example.ui.components.InteractiveWeatherWidget
import com.example.ui.components.LauncherSettingsSheet
import com.example.ui.components.ParallaxWallpaperLayer
import com.example.ui.components.TopControlCenterOverlay
import com.example.ui.components.TransparentTopStatusStrip
import com.example.ui.theme.LuminaCyan
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
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

    // Live clock & Indonesian date ticker
    var timeLabel by remember { mutableStateOf(formatCurrentTime()) }
    var dateLabel by remember { mutableStateOf(formatCurrentDate()) }
    LaunchedEffect(Unit) {
        while (true) {
            timeLabel = formatCurrentTime()
            dateLabel = formatCurrentDate()
            delay(15_000L)
        }
    }

    // Snackbar feedback observer
    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    // Zero-permission Android Photo Picker for custom gallery wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    // Optional coarse location runtime permission for live GPS weather
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.selectWeatherCity(state.preferences.selectedCityId, useGps = true)
        } else {
            viewModel.refreshWeather(state.preferences.selectedCityId, useGps = false)
        }
    }

    val pageCount = state.homePages.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    val pagerScrollFraction = remember(
        pagerState.currentPage,
        pagerState.currentPageOffsetFraction,
        pageCount
    ) {
        if (pageCount <= 1) 0.5f
        else ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pageCount - 1).toFloat())
            .coerceIn(0f, 1f)
    }

    val isOverlayOpen = state.isControlCenterOpen ||
        state.isAppDrawerOpen ||
        state.isSettingsOpen ||
        state.active3DApp != null

    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isOverlayOpen) {
                if (!isOverlayOpen) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 34f) {
                            viewModel.setControlCenterOpen(true)
                        } else if (dragAmount < -34f) {
                            viewModel.setAppDrawerOpen(true)
                        }
                    }
                }
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
            isBackdropBlurred = isOverlayOpen
        )

        // 2. Main Home Screen Layout (Edge-to-Edge)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Clean Transparent iPhone-style Status & Control Strip
            TransparentTopStatusStrip(
                timeLabel = timeLabel,
                deviceStatus = state.deviceStatus,
                onOpenControlCenter = { viewModel.setControlCenterOpen(true) },
                onOpenSettings = { viewModel.setSettingsOpen(true) }
            )

            // Interactive 3D Pager for Home Pages
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

                val pageApps = state.homePages.getOrElse(pageIndex) { emptyList() }

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
                        FirstHomePageContent(
                            timeLabel = timeLabel,
                            dateLabel = dateLabel,
                            apps = pageApps,
                            gridColumns = state.preferences.gridColumns,
                            iconStyle = state.preferences.iconStyle,
                            useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                            weatherState = state.weatherState,
                            selectedCityId = state.preferences.selectedCityId,
                            useGpsWeather = state.preferences.useGpsWeather,
                            tiltX = tilt.first,
                            tiltY = tilt.second,
                            onRefreshWeather = { viewModel.refreshWeather() },
                            onSelectCity = { viewModel.selectWeatherCity(it, useGps = false) },
                            onRequestGps = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            },
                            onLaunchApp = { viewModel.launchApp(it) },
                            onLongPressApp = { viewModel.setActive3DApp(it) }
                        )
                    } else {
                        SecondaryHomePageContent(
                            pageNumber = pageIndex + 1,
                            apps = pageApps,
                            gridColumns = state.preferences.gridColumns,
                            iconStyle = state.preferences.iconStyle,
                            useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                            tiltX = tilt.first,
                            tiltY = tilt.second,
                            onLaunchApp = { viewModel.launchApp(it) },
                            onLongPressApp = { viewModel.setActive3DApp(it) }
                        )
                    }
                }
            }

            // Minimalist Glowing Page Indicator + Swipe-Up Drawer Hint
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                repeat(pageCount) { idx ->
                    val isSelected = pagerState.currentPage == idx
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 7.dp,
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

            // Floating Frosted Glass 3D Dock
            FloatingGlassDock(
                dockApps = state.dockApps,
                iconStyle = state.preferences.iconStyle,
                useCustom3DGlyphs = state.preferences.useCustom3DGlyphs,
                tiltX = tilt.first,
                tiltY = tilt.second,
                onLaunchApp = { viewModel.launchApp(it) },
                onLongPressApp = { viewModel.setActive3DApp(it) },
                onOpenAppDrawer = { viewModel.setAppDrawerOpen(true) },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .widthIn(max = 540.dp)
            )
        }

        // 3. Top Transparent iPhone-style Control Center Overlay
        TopControlCenterOverlay(
            visible = state.isControlCenterOpen,
            timeLabel = timeLabel,
            dateLabel = dateLabel,
            deviceStatus = state.deviceStatus,
            wallpaperPreset = state.preferences.wallpaperPreset,
            iconStyle = state.preferences.iconStyle,
            onDismiss = { viewModel.setControlCenterOpen(false) },
            onBrightnessChange = { viewModel.setWindowBrightness(it) },
            onVolumeChange = { viewModel.setMediaVolume(it) },
            onToggleTorch = { viewModel.toggleFlashlight() },
            onCycleWallpaper = { viewModel.cycleWallpaperPreset() },
            onCycleIconStyle = { viewModel.cycleIconStyle() },
            onOpenInternet = { viewModel.openInternetPanel() },
            onOpenBluetooth = { viewModel.openBluetoothSettings() },
            onOpenSystemSettings = { viewModel.openSystemSettings() },
            onOpenLauncherSettings = { viewModel.setSettingsOpen(true) },
            onOpenDefaultHome = { viewModel.openDefaultHomeSettings() }
        )

        // 4. App Drawer Sheet
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

        // 5. Full Launcher Customization Sheet (with pinned transparent top bar)
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
            onSelectIconStyle = { viewModel.setIconStyle(it) },
            onToggleCustom3DGlyphs = { viewModel.setUseCustom3DGlyphs(it) },
            onSelectPageTransition = { viewModel.setPageTransition(it) },
            onSelectGridColumns = { viewModel.setGridColumns(it) },
            onUnhideApp = { viewModel.toggleHideApp(it) }
        )

        // 6. Interactive 3D Hold App Overlay ("menyentuh aplikasi saat ditahan")
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
                .navigationBarsPadding()
                .padding(bottom = 104.dp, start = 20.dp, end = 20.dp)
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
private fun FirstHomePageContent(
    timeLabel: String,
    dateLabel: String,
    apps: List<InstalledApp>,
    gridColumns: Int,
    iconStyle: com.example.data.IconStyle3D,
    useCustom3DGlyphs: Boolean,
    weatherState: com.example.data.WeatherUiState,
    selectedCityId: String,
    useGpsWeather: Boolean,
    tiltX: Float,
    tiltY: Float,
    onRefreshWeather: () -> Unit,
    onSelectCity: (String) -> Unit,
    onRequestGps: () -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimalist Hero Clock & Greeting Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.80f)
            )
        }

        // Integrated Interactive Weather Widget with Smooth Transitions
        InteractiveWeatherWidget(
            weatherState = weatherState,
            selectedCityId = selectedCityId,
            useGpsWeather = useGpsWeather,
            onRefresh = onRefreshWeather,
            onSelectCity = onSelectCity,
            onRequestGpsLocation = onRequestGps
        )

        // Curated 3D App Grid on Page 0
        AppGridRows(
            apps = apps,
            columns = gridColumns,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            tiltX = tiltX,
            tiltY = tiltY,
            onLaunchApp = onLaunchApp,
            onLongPressApp = onLongPressApp
        )
    }
}

@Composable
private fun SecondaryHomePageContent(
    pageNumber: Int,
    apps: List<InstalledApp>,
    gridColumns: Int,
    iconStyle: com.example.data.IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 14.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = "Halaman $pageNumber",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "Tahan ikon untuk efek 3D",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.56f)
            )
        }

        AppGridRows(
            apps = apps,
            columns = gridColumns,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            tiltX = tiltX,
            tiltY = tiltY,
            onLaunchApp = onLaunchApp,
            onLongPressApp = onLongPressApp
        )
    }
}

@Composable
private fun AppGridRows(
    apps: List<InstalledApp>,
    columns: Int,
    iconStyle: com.example.data.IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit
) {
    val rows = remember(apps, columns) { apps.chunked(columns) }
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { rowApps ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowApps.forEach { app ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        AppIcon3DItem(
                            app = app,
                            iconStyle = iconStyle,
                            useCustom3DGlyphs = useCustom3DGlyphs,
                            showLabel = true,
                            iconSize = if (columns == 5) 52.dp else 58.dp,
                            tiltX = tiltX,
                            tiltY = tiltY,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onLongPressApp(app) }
                        )
                    }
                }
                val emptySlots = columns - rowApps.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FloatingGlassDock(
    dockApps: List<InstalledApp>,
    iconStyle: com.example.data.IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tiltX: Float,
    tiltY: Float,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onOpenAppDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(30.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .clip(dockShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.11f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.14f)
                    )
                ),
                shape = dockShape
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("floating_glass_dock")
    ) {
        dockApps.take(4).forEach { app ->
            AppIcon3DItem(
                app = app,
                iconStyle = iconStyle,
                useCustom3DGlyphs = useCustom3DGlyphs,
                showLabel = false,
                iconSize = 54.dp,
                tiltX = tiltX,
                tiltY = tiltY,
                onClick = { onLaunchApp(app) },
                onLongClick = { onLongPressApp(app) }
            )
        }

        // Dedicated minimalist 3D App Drawer orb button
        val drawerShape = RoundedCornerShape(17.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(drawerShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            LuminaCyan.copy(alpha = 0.34f),
                            Color(0xFF4F8BFF).copy(alpha = 0.28f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.45f), drawerShape)
                .clickable(onClick = onOpenAppDrawer)
                .testTag("dock_open_drawer_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = stringResource(R.string.cd_app_drawer),
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

private fun formatCurrentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun formatCurrentDate(): String =
    SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
