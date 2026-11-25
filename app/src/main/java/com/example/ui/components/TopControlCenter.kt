package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DeviceStatus
import com.example.data.IconStyle3D
import com.example.data.TopShadeMode
import com.example.data.WallpaperPreset
import com.example.data.WeatherUiState
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCobalt
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaMint
import java.util.Locale

/**
 * Full-screen iPhone 18 Pro Max Control Center & Notification Center shade.
 * Floats cleanly over the blurred home screen so ONLY controls or notifications are visible.
 */
@Composable
fun TopControlCenterOverlay(
    mode: TopShadeMode,
    timeLabel: String,
    dateLabel: String,
    deviceStatus: DeviceStatus,
    weatherState: WeatherUiState,
    wallpaperPreset: WallpaperPreset,
    iconStyle: IconStyle3D,
    onSwitchMode: (TopShadeMode) -> Unit,
    onDismiss: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleTorch: () -> Unit,
    onCycleWallpaper: () -> Unit,
    onCycleIconStyle: () -> Unit,
    onOpenInternet: () -> Unit,
    onOpenAirplaneMode: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenDefaultHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = mode != TopShadeMode.CLOSED
    if (isVisible) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it / 4 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x9E060912),
                            Color(0xB8050810)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -26f) onDismiss()
                    }
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("top_control_center_overlay")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                // Clean unified top segmented switcher: Pusat Kontrol | Notifikasi + Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val segShape = RoundedCornerShape(22.dp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(segShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.24f), segShape)
                            .padding(4.dp)
                    ) {
                        ShadeModeTab(
                            label = "Pusat Kontrol",
                            selected = mode == TopShadeMode.CONTROL_CENTER,
                            tag = "shade_tab_control",
                            onClick = { onSwitchMode(TopShadeMode.CONTROL_CENTER) }
                        )
                        ShadeModeTab(
                            label = "Notifikasi",
                            selected = mode == TopShadeMode.NOTIFICATIONS,
                            tag = "shade_tab_notifications",
                            onClick = { onSwitchMode(TopShadeMode.NOTIFICATIONS) }
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("close_control_center_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close_panel),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Body smoothly switches between Control Center and Notification Center
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f),
                    label = "top_shade_mode_switch"
                ) { currentMode ->
                    if (currentMode == TopShadeMode.NOTIFICATIONS) {
                        NotificationCenterContent(
                            timeLabel = timeLabel,
                            dateLabel = dateLabel,
                            deviceStatus = deviceStatus,
                            weatherState = weatherState,
                            onOpenSystemNotifications = onOpenNotificationSettings,
                            onOpenLauncherSettings = onOpenLauncherSettings
                        )
                    } else {
                        ControlCenterTilesContent(
                            deviceStatus = deviceStatus,
                            wallpaperPreset = wallpaperPreset,
                            iconStyle = iconStyle,
                            onBrightnessChange = onBrightnessChange,
                            onVolumeChange = onVolumeChange,
                            onToggleTorch = onToggleTorch,
                            onCycleWallpaper = onCycleWallpaper,
                            onCycleIconStyle = onCycleIconStyle,
                            onOpenInternet = onOpenInternet,
                            onOpenAirplaneMode = onOpenAirplaneMode,
                            onOpenBluetooth = onOpenBluetooth,
                            onOpenSystemSettings = onOpenSystemSettings,
                            onOpenLauncherSettings = onOpenLauncherSettings,
                            onOpenDefaultHome = onOpenDefaultHome
                        )
                    }
                }

                // Bottom swipe-up close indicator pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp)
                        .width(46.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f))
                )
            }
        }
    }
}

@Composable
private fun ShadeModeTab(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Color.White.copy(alpha = 0.24f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.68f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun ControlCenterTilesContent(
    deviceStatus: DeviceStatus,
    wallpaperPreset: WallpaperPreset,
    iconStyle: IconStyle3D,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleTorch: () -> Unit,
    onCycleWallpaper: () -> Unit,
    onCycleIconStyle: () -> Unit,
    onOpenInternet: () -> Unit,
    onOpenAirplaneMode: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenDefaultHome: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Row 1: Connectivity Quad (Airplane, Wi-Fi, Bluetooth, Data) + Style Cluster
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GlassControlTile(modifier = Modifier.weight(1f)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularGlassToggle(
                            icon = Icons.Rounded.AirplanemodeActive,
                            label = "Pesawat",
                            isActive = deviceStatus.isAirplaneModeOn,
                            activeColor = LuminaAmber,
                            tag = "cc_airplane_toggle",
                            onClick = onOpenAirplaneMode
                        )
                        CircularGlassToggle(
                            icon = Icons.Rounded.Wifi,
                            label = "Wi-Fi",
                            isActive = deviceStatus.isWifiConnected,
                            activeColor = LuminaCobalt,
                            tag = "cc_wifi_toggle",
                            onClick = onOpenInternet
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularGlassToggle(
                            icon = Icons.Rounded.Bluetooth,
                            label = "Bluetooth",
                            isActive = false,
                            activeColor = LuminaCobalt,
                            tag = "cc_bluetooth_toggle",
                            onClick = onOpenBluetooth
                        )
                        CircularGlassToggle(
                            icon = Icons.Rounded.SignalCellularAlt,
                            label = "Data",
                            isActive = deviceStatus.isCellularConnected,
                            activeColor = LuminaMint,
                            tag = "cc_cellular_toggle",
                            onClick = onOpenInternet
                        )
                    }
                }
            }

            GlassControlTile(modifier = Modifier.weight(1f)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickStyleActionRow(
                        icon = Icons.Rounded.Wallpaper,
                        title = "Wallpaper",
                        value = wallpaperPreset.title,
                        accent = LuminaCyan,
                        tag = "cc_cycle_wallpaper",
                        onClick = onCycleWallpaper
                    )
                    QuickStyleActionRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Ikon 3D",
                        value = iconStyle.label,
                        accent = LuminaIndigo,
                        tag = "cc_cycle_icon_style",
                        onClick = onCycleIconStyle
                    )
                }
            }
        }

        // Row 2: Frosted Glass Brightness & Volume Sliders
        GlassControlTile(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FrostedSliderRow(
                    icon = Icons.Rounded.Brightness6,
                    label = "Kecerahan",
                    value = deviceStatus.screenBrightnessFraction,
                    accent = LuminaAmber,
                    onValueChange = onBrightnessChange,
                    tag = "cc_brightness_slider"
                )
                FrostedSliderRow(
                    icon = Icons.Rounded.VolumeUp,
                    label = "Volume Media",
                    value = deviceStatus.mediaVolumeFraction,
                    accent = LuminaCyan,
                    onValueChange = onVolumeChange,
                    tag = "cc_volume_slider"
                )
            }
        }

        // Row 3: Quick Utility Glass Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            UtilityGlassPill(
                icon = if (deviceStatus.isTorchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                label = if (deviceStatus.isTorchOn) "Senter On" else "Senter",
                isActive = deviceStatus.isTorchOn,
                activeColor = LuminaAmber,
                tag = "cc_torch_button",
                onClick = onToggleTorch,
                modifier = Modifier.weight(1f)
            )
            UtilityGlassPill(
                icon = Icons.Rounded.Palette,
                label = "Lumina",
                isActive = false,
                activeColor = LuminaCyan,
                tag = "cc_launcher_settings_button",
                onClick = onOpenLauncherSettings,
                modifier = Modifier.weight(1f)
            )
            UtilityGlassPill(
                icon = Icons.Rounded.Home,
                label = if (deviceStatus.isDefaultLauncher) "Default" else "Set Utama",
                isActive = deviceStatus.isDefaultLauncher,
                activeColor = LuminaMint,
                tag = "cc_default_home_button",
                onClick = onOpenDefaultHome,
                modifier = Modifier.weight(1f)
            )
            UtilityGlassPill(
                icon = Icons.Rounded.Settings,
                label = "Sistem",
                isActive = false,
                activeColor = Color.White,
                tag = "cc_system_settings",
                onClick = onOpenSystemSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NotificationCenterContent(
    timeLabel: String,
    dateLabel: String,
    deviceStatus: DeviceStatus,
    weatherState: WeatherUiState,
    onOpenSystemNotifications: () -> Unit,
    onOpenLauncherSettings: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.80f)
        )
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp),
            color = Color.White,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Live Weather Summary Notification Card
        val weatherSuccess = weatherState as? WeatherUiState.Success
        if (weatherSuccess != null) {
            NotificationGlassCard(
                icon = Icons.Rounded.WbSunny,
                iconTint = LuminaAmber,
                title = "Cuaca ${weatherSuccess.data.cityName} • ${weatherSuccess.data.temperatureC}°",
                body = "${weatherSuccess.data.conditionLabel}, terasa ${weatherSuccess.data.feelsLikeC}°. Tertinggi ${weatherSuccess.data.highTempC}° / Terendah ${weatherSuccess.data.lowTempC}°.",
                timeTag = weatherSuccess.data.updatedAtLabel,
                onClick = {}
            )
        }

        // Live Poco F7 Battery & RAM Status Card
        NotificationGlassCard(
            icon = if (deviceStatus.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
            iconTint = if (deviceStatus.isCharging) LuminaMint else LuminaCyan,
            title = if (deviceStatus.isCharging) "Sedang Mengisi Daya (${deviceStatus.batteryPercent}%)" else "Status Perangkat Poco F7",
            body = String.format(
                Locale.US,
                "Baterai %d%% • RAM tersedia %.1f GB dari %.0f GB • %s",
                deviceStatus.batteryPercent,
                deviceStatus.availableRamGb,
                deviceStatus.totalRamGb,
                when {
                    deviceStatus.isAirplaneModeOn -> "Mode Pesawat Aktif"
                    deviceStatus.isWifiConnected -> "Terhubung Wi-Fi"
                    deviceStatus.isCellularConnected -> "Data Seluler Aktif"
                    else -> "Offline"
                }
            ),
            timeTag = "Aktif",
            onClick = {}
        )

        // System Notification Shortcut Card
        NotificationGlassCard(
            icon = Icons.Rounded.Notifications,
            iconTint = LuminaIndigo,
            title = "Riwayat & Pengaturan Notifikasi Aplikasi",
            body = "Ketuk untuk membuka pengaturan notifikasi sistem Poco F7 (WhatsApp, pesan, dan aplikasi lainnya).",
            timeTag = "Sistem",
            onClick = onOpenSystemNotifications
        )

        // Launcher Quick Customize Hint
        NotificationGlassCard(
            icon = Icons.Rounded.Palette,
            iconTint = LuminaCyan,
            title = "Kustomisasi Lumina F7",
            body = "Atur wallpaper parallax, gaya ikon 3D, transisi halaman, dan navigasi gestur.",
            timeTag = "Lumina",
            onClick = onOpenLauncherSettings
        )
    }
}

@Composable
private fun NotificationGlassCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    timeTag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.13f))
            .border(0.8.dp, Color.White.copy(alpha = 0.24f), shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconTint.copy(alpha = 0.22f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.58f)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun GlassControlTile(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.13f))
            .border(0.8.dp, Color.White.copy(alpha = 0.25f), shape)
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun CircularGlassToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.White.copy(alpha = 0.15f))
                .border(
                    0.8.dp,
                    if (isActive) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.20f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun QuickStyleActionRow(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.24f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FrostedSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    accent: Color,
    onValueChange: (Float) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accent,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f)
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(tag)
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.84f),
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun UtilityGlassPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(shape)
            .background(if (isActive) activeColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.13f))
            .border(
                0.8.dp,
                if (isActive) activeColor.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.22f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else Color.White,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1
        )
    }
}
