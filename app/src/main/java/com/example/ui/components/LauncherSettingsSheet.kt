package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.IconStyle3D
import com.example.data.InstalledApp
import com.example.data.LauncherPreferences
import com.example.data.PageTransition3D
import com.example.data.WallpaperPreset
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaMint

@Composable
fun LauncherSettingsSheet(
    visible: Boolean,
    preferences: LauncherPreferences,
    isDefaultLauncher: Boolean,
    hiddenApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onOpenDefaultHome: () -> Unit,
    onSelectWallpaperPreset: (WallpaperPreset) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onParallaxIntensityChange: (Float) -> Unit,
    onAmbientGlowChange: (Boolean) -> Unit,
    onImmersiveGestureChange: (Boolean) -> Unit,
    onOpenGestureSystemSettings: () -> Unit,
    onSelectIconStyle: (IconStyle3D) -> Unit,
    onToggleCustom3DGlyphs: (Boolean) -> Unit,
    onSelectPageTransition: (PageTransition3D) -> Unit,
    onSelectGridColumns: (Int) -> Unit,
    onUnhideApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCF0A0F1C),
                            Color(0xEB070A12)
                        )
                    )
                )
                .testTag("launcher_settings_sheet")
        ) {
            // Scrollable Settings Body gliding underneath the clean transparent iPhone-like top bar
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 112.dp,
                    bottom = 44.dp,
                    start = 18.dp,
                    end = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // 1. Safe Default Launcher Setup for Poco F7
                item {
                    SettingsGlassSection(title = "Aktivasi Launcher Poco F7") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LuminaMint.copy(alpha = 0.20f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = LuminaMint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isDefaultLauncher) "Lumina F7 Aktif sebagai Utama" else "Siap Digunakan & 100% Aman",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tanpa izin berbahaya, tanpa root. Dapat kembali ke Poco Launcher kapan saja.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenDefaultHome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaCyan,
                                contentColor = Color(0xFF06101B)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_set_default_launcher")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = stringResource(R.string.cd_default_launcher),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDefaultLauncher) "Ubah Pengaturan Launcher Utama" else "Jadikan Launcher Utama",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1B. Gesture Navigation Mode (Replace 3-button bar with iPhone Gesture Pill)
                item {
                    SettingsGlassSection(title = "Navigasi Gestur (Ala iPhone 18 Pro Max)") {
                        SettingsSwitchRow(
                            title = "Mode Gestur Penuh di Launcher",
                            subtitle = "Sembunyikan 3 tombol navigasi. Geser tepi kiri/kanan untuk kembali, geser atas untuk aplikasi, geser kiri beranda untuk Mi Browser.",
                            checked = preferences.immersiveGestureNav,
                            onCheckedChange = onImmersiveGestureChange,
                            tag = "settings_immersive_gesture_switch"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onOpenGestureSystemSettings)
                                .padding(vertical = 11.dp, horizontal = 12.dp)
                                .testTag("settings_open_system_gesture")
                        ) {
                            Text(
                                text = "Buka Pengaturan Gestur Sistem Poco F7",
                                style = MaterialTheme.typography.labelLarge,
                                color = LuminaCyan
                            )
                        }
                    }
                }

                // 2. Parallax Wallpaper Studio
                item {
                    SettingsGlassSection(title = "Wallpaper Parallax Dinamis") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WallpaperPreset.entries.forEach { preset ->
                                val isSelected = preferences.customWallpaperUri == null &&
                                    preferences.wallpaperPreset == preset
                                val shape = RoundedCornerShape(18.dp)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(118.dp)
                                        .clip(shape)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.8.dp,
                                            color = if (isSelected) LuminaCyan else Color.White.copy(alpha = 0.22f),
                                            shape = shape
                                        )
                                        .clickable { onSelectWallpaperPreset(preset) }
                                        .testTag("settings_wall_${preset.id}")
                                ) {
                                    Image(
                                        painter = painterResource(id = preset.drawableRes),
                                        contentDescription = preset.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                                )
                                            )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = LuminaCyan,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onPickCustomWallpaper)
                                .padding(vertical = 12.dp)
                                .testTag("settings_pick_gallery_wallpaper")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = LuminaCyan,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Wallpaper dari Galeri",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kedalaman Parallax Halus",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "${(preferences.parallaxIntensity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = LuminaCyan
                            )
                        }
                        Slider(
                            value = preferences.parallaxIntensity,
                            onValueChange = onParallaxIntensityChange,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = LuminaCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("settings_parallax_slider")
                        )

                        SettingsSwitchRow(
                            title = "Kilau Cahaya Dinamis",
                            subtitle = "Efek aurora lembut yang bergerak pelan di latar",
                            checked = preferences.ambientGlowEnabled,
                            onCheckedChange = onAmbientGlowChange,
                            tag = "settings_ambient_glow_switch"
                        )
                    }
                }

                // 3. 3D Minimalist Icons
                item {
                    SettingsGlassSection(title = "Gaya Ikon Kustom 3D") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconStyle3D.entries.forEach { style ->
                                SettingsRadioOptionRow(
                                    title = style.label,
                                    subtitle = style.subtitle,
                                    selected = preferences.iconStyle == style,
                                    onClick = { onSelectIconStyle(style) },
                                    tag = "settings_icon_${style.name}"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsSwitchRow(
                            title = "Ikon Bawaan Gaya iPhone",
                            subtitle = "Browser, Telepon, Pesan, Kamera & sistem bergaya iPhone; AI, Game & aplikasi lain tetap pakai ikon aslinya.",
                            checked = preferences.useCustom3DGlyphs,
                            onCheckedChange = onToggleCustom3DGlyphs,
                            tag = "settings_custom_glyphs_switch"
                        )
                    }
                }

                // 4. 3D Page Transitions
                item {
                    SettingsGlassSection(title = "Transisi 3D Antar Halaman") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PageTransition3D.entries.forEach { mode ->
                                SettingsRadioOptionRow(
                                    title = mode.label,
                                    subtitle = mode.subtitle,
                                    selected = preferences.pageTransition == mode,
                                    onClick = { onSelectPageTransition(mode) },
                                    tag = "settings_transition_${mode.name}"
                                )
                            }
                        }
                    }
                }

                // 5. Grid Density
                item {
                    SettingsGlassSection(title = "Tata Letak Layar Beranda") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(4 to "4 Kolom (Minimalis)", 5 to "5 Kolom (Lega)").forEach { (cols, label) ->
                                val selected = preferences.gridColumns == cols
                                val shape = RoundedCornerShape(16.dp)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(shape)
                                        .background(
                                            if (selected) LuminaCyan.copy(alpha = 0.24f)
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) LuminaCyan else Color.White.copy(alpha = 0.16f),
                                            shape
                                        )
                                        .clickable { onSelectGridColumns(cols) }
                                        .padding(vertical = 13.dp)
                                        .testTag("settings_grid_$cols")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Hidden Apps Restore
                if (hiddenApps.isNotEmpty()) {
                    item {
                        SettingsGlassSection(title = "Aplikasi Tersembunyi (${hiddenApps.size})") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                hiddenApps.forEach { app ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.07f))
                                            .clickable { onUnhideApp(app) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Visibility,
                                                contentDescription = "Tampilkan",
                                                tint = LuminaCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Tampilkan",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = LuminaCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pinned Clean & Transparent iPhone-style Top Settings Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xBB0C1220),
                                Color(0x800C1220),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                val barShape = RoundedCornerShape(24.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(barShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(0.9.dp, Color.White.copy(alpha = 0.30f), barShape)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "Pengaturan Lumina",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tampilan, 3D & Wallpaper Parallax",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(onClick = onDismiss)
                            .testTag("close_settings_button")
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
        }
    }
}

@Composable
private fun SettingsGlassSection(
    title: String,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.09f))
            .border(0.8.dp, Color.White.copy(alpha = 0.20f), shape)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingsRadioOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) LuminaCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
            .border(
                0.9.dp,
                if (selected) LuminaCyan.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.12f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag(tag)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.68f)
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = LuminaCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.66f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LuminaCyan,
                uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.20f)
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
