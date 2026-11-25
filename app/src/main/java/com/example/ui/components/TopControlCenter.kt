package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
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
import com.example.R
import com.example.data.DeviceStatus
import com.example.data.IconStyle3D
import com.example.data.WallpaperPreset
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCobalt
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaMint

@Composable
fun TransparentTopStatusStrip(
    timeLabel: String,
    deviceStatus: DeviceStatus,
    onOpenControlCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 16f) onOpenControlCenter()
                }
            }
    ) {
        // Left floating translucent glass time & device badge
        val pillShape = RoundedCornerShape(22.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(pillShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(0.8.dp, Color.White.copy(alpha = 0.30f), pillShape)
                .clickable(onClick = onOpenControlCenter)
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("top_transparent_status_pill")
        ) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(LuminaCyan)
            )
            Icon(
                imageVector = if (deviceStatus.isWifiConnected) Icons.Rounded.Wifi else Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.size(14.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = if (deviceStatus.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                    contentDescription = null,
                    tint = if (deviceStatus.isCharging) LuminaMint else Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${deviceStatus.batteryPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }

        // Right iPhone-inspired transparent quick control & settings pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(pillShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.32f), pillShape)
                    .clickable(onClick = onOpenControlCenter)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("open_control_center_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.cd_open_control_center),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Kontrol",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.32f), CircleShape)
                    .clickable(onClick = onOpenSettings)
                    .testTag("open_settings_top_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.cd_open_settings),
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun TopControlCenterOverlay(
    visible: Boolean,
    timeLabel: String,
    dateLabel: String,
    deviceStatus: DeviceStatus,
    wallpaperPreset: WallpaperPreset,
    iconStyle: IconStyle3D,
    onDismiss: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleTorch: () -> Unit,
    onCycleWallpaper: () -> Unit,
    onCycleIconStyle: () -> Unit,
    onOpenInternet: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenDefaultHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -22f) onDismiss()
                    }
                }
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("top_control_center_overlay")
        ) {
            val containerShape = RoundedCornerShape(32.dp)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .clip(containerShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x88141B2D),
                                Color(0xAA0C111E)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.25f)
                            )
                        ),
                        shape = containerShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Clean Transparent iPhone-style Top Header inside Control Center
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$dateLabel  •  Poco F7",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (deviceStatus.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                                contentDescription = null,
                                tint = if (deviceStatus.isCharging) LuminaMint else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${deviceStatus.batteryPercent}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
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
                }

                // Row 1: Connectivity Quad + Style Quick Island
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Connectivity Glass Tile
                    GlassControlTile(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularGlassToggle(
                                    icon = Icons.Rounded.Wifi,
                                    label = "Wi-Fi",
                                    isActive = deviceStatus.isWifiConnected,
                                    activeColor = LuminaCobalt,
                                    tag = "cc_wifi_toggle",
                                    onClick = onOpenInternet
                                )
                                CircularGlassToggle(
                                    icon = Icons.Rounded.Bluetooth,
                                    label = "Bluetooth",
                                    isActive = false,
                                    activeColor = LuminaCobalt,
                                    tag = "cc_bluetooth_toggle",
                                    onClick = onOpenBluetooth
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularGlassToggle(
                                    icon = Icons.Rounded.SignalCellularAlt,
                                    label = "Data",
                                    isActive = deviceStatus.isCellularConnected,
                                    activeColor = LuminaMint,
                                    tag = "cc_cellular_toggle",
                                    onClick = onOpenInternet
                                )
                                CircularGlassToggle(
                                    icon = Icons.Rounded.Settings,
                                    label = "Sistem",
                                    isActive = false,
                                    activeColor = LuminaCyan,
                                    tag = "cc_system_settings",
                                    onClick = onOpenSystemSettings
                                )
                            }
                        }
                    }

                    // Dynamic 3D Look Quick Island
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
                                accent = Color(0xFFA78BFA),
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

                // Row 3: Quick Utility Glass Pills (Torch, Launcher Settings, Default Home)
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
                        label = "Kustomisasi",
                        isActive = false,
                        activeColor = LuminaCyan,
                        tag = "cc_launcher_settings_button",
                        onClick = onOpenLauncherSettings,
                        modifier = Modifier.weight(1f)
                    )
                    UtilityGlassPill(
                        icon = Icons.Rounded.Home,
                        label = if (deviceStatus.isDefaultLauncher) "Aktif" else "Set Default",
                        isActive = deviceStatus.isDefaultLauncher,
                        activeColor = LuminaMint,
                        tag = "cc_default_home_button",
                        onClick = onOpenDefaultHome,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bottom clean swipe-up handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 2.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}

@Composable
private fun GlassControlTile(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.11f))
            .border(0.8.dp, Color.White.copy(alpha = 0.22f), shape)
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
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.White.copy(alpha = 0.14f))
                .border(
                    0.8.dp,
                    if (isActive) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.18f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.82f)
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
            .background(Color.White.copy(alpha = 0.09f))
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
                inactiveTrackColor = Color.White.copy(alpha = 0.20f)
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(tag)
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.80f),
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
            .background(if (isActive) activeColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.11f))
            .border(
                0.8.dp,
                if (isActive) activeColor.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.20f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
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
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1
        )
    }
}
