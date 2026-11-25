package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppCategory
import com.example.data.DeviceStatus
import com.example.data.IconStyle3D
import com.example.data.InstalledApp
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCobalt
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaMint
import java.util.Locale

@Composable
fun DynamicIslandTopBar(
    onOpenUnifiedTopShade: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Clean single unified top touch/swipe zone that does NOT stack a second Dynamic Island
    // under Poco F7's native camera island. Tap or swipe down to open the unified Control & Notification shade.
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(26.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpenUnifiedTopShade
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 12f) {
                        onOpenUnifiedTopShade()
                    }
                }
            }
            .testTag("unified_top_shade_handle")
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.28f))
        )
    }
}

@Composable
fun ProMaxHeroClockHeader(
    timeLabel: String,
    dateLabel: String,
    onClockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClockClick)
            .padding(vertical = 4.dp)
            .testTag("promax_hero_clock")
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.84f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 68.sp,
                lineHeight = 72.sp,
                letterSpacing = (-2.4).sp
            ),
            color = Color.White.copy(alpha = 0.96f),
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun ProMaxBentoWidgetRow(
    deviceStatus: DeviceStatus,
    dayOfWeekLabel: String,
    dayOfMonthLabel: String,
    monthYearLabel: String,
    onBatteryCardClick: () -> Unit,
    onCalendarCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Left Liquid-Glass Battery & Poco F7 Performance Ring Widget
        val cardShape = RoundedCornerShape(26.dp)
        val batteryFraction = (deviceStatus.batteryPercent / 100f).coerceIn(0.02f, 1f)
        val ringColor = when {
            deviceStatus.isCharging -> LuminaMint
            deviceStatus.batteryPercent <= 20 -> LuminaCoral
            else -> LuminaCyan
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .clip(cardShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x40FFFFFF), Color(0x1CFFFFFF))
                    )
                )
                .border(0.9.dp, Color.White.copy(alpha = 0.30f), cardShape)
                .clickable(onClick = onBatteryCardClick)
                .padding(14.dp)
                .testTag("bento_battery_widget")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    val inset = 3.dp.toPx()
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    drawArc(
                        color = Color.White.copy(alpha = 0.16f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * batteryFraction,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke
                    )
                }
                Icon(
                    imageVector = if (deviceStatus.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = ringColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${deviceStatus.batteryPercent}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.US, "RAM %.1f GB", deviceStatus.availableRamGb),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.74f),
                    maxLines = 1
                )
            }
        }

        // Right Liquid-Glass iPhone Calendar Widget
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .clip(cardShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x40FFFFFF), Color(0x1CFFFFFF))
                    )
                )
                .border(0.9.dp, Color.White.copy(alpha = 0.30f), cardShape)
                .clickable(onClick = onCalendarCardClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("bento_calendar_widget")
        ) {
            Text(
                text = dayOfWeekLabel.uppercase(Locale("id", "ID")),
                style = MaterialTheme.typography.labelMedium,
                color = LuminaCoral,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayOfMonthLabel,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = monthYearLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(bottom = 5.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SmartCuratedFoldersRow(
    aiApps: List<InstalledApp>,
    gameApps: List<InstalledApp>,
    essentialApps: List<InstalledApp>,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    onOpenFolder: (AppCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SmartGlassFolderCard(
            title = "AI",
            apps = aiApps,
            accentColor = LuminaCyan,
            fallbackIcon = Icons.Rounded.AutoAwesome,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            tag = "folder_card_ai",
            onClick = { onOpenFolder(AppCategory.AI) },
            modifier = Modifier.weight(1f)
        )
        SmartGlassFolderCard(
            title = "Game",
            apps = gameApps,
            accentColor = LuminaIndigo,
            fallbackIcon = Icons.Rounded.SportsEsports,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            tag = "folder_card_game",
            onClick = { onOpenFolder(AppCategory.GAMES) },
            modifier = Modifier.weight(1f)
        )
        SmartGlassFolderCard(
            title = "Penting",
            apps = essentialApps,
            accentColor = LuminaAmber,
            fallbackIcon = Icons.Rounded.Star,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            tag = "folder_card_essential",
            onClick = { onOpenFolder(AppCategory.ESSENTIALS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SmartGlassFolderCard(
    title: String,
    apps: List<InstalledApp>,
    accentColor: Color,
    fallbackIcon: ImageVector,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.09f)
                    )
                )
            )
            .border(
                width = 0.9.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        accentColor.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag(tag)
    ) {
        // 2x2 Mini 3D Icon Preview inside the folder
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.22f))
                .padding(6.dp)
        ) {
            if (apps.isEmpty()) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                val preview = apps.take(4)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniFolderAppSlot(preview.getOrNull(0), iconStyle, useCustom3DGlyphs)
                        MiniFolderAppSlot(preview.getOrNull(1), iconStyle, useCustom3DGlyphs)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniFolderAppSlot(preview.getOrNull(2), iconStyle, useCustom3DGlyphs)
                        MiniFolderAppSlot(preview.getOrNull(3), iconStyle, useCustom3DGlyphs)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1
            )
            if (apps.isNotEmpty()) {
                Text(
                    text = "${apps.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun MiniFolderAppSlot(
    app: InstalledApp?,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean
) {
    if (app == null) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        return
    }
    val customGlyph = remember(app.packageName, app.label, useCustom3DGlyphs) {
        if (useCustom3DGlyphs) resolveCustom3DGlyph(app) else null
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                if (customGlyph != null) {
                    Brush.linearGradient(listOf(customGlyph.topColor, customGlyph.bottomColor))
                } else {
                    Brush.linearGradient(listOf(Color(0x44FFFFFF), Color(0x22FFFFFF)))
                }
            )
    ) {
        if (customGlyph != null) {
            Icon(
                imageVector = customGlyph.icon,
                contentDescription = null,
                tint = if (iconStyle == IconStyle3D.OBSIDIAN_3D) customGlyph.topColor else Color.White,
                modifier = Modifier.size(14.dp)
            )
        } else if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = app.label.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun LiquidGlassFolderPopup(
    category: AppCategory?,
    apps: List<InstalledApp>,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    onDismiss: () -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onOpenAllDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (category != null) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = category != null,
        enter = fadeIn() + scaleIn(
            initialScale = 0.86f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.90f),
        modifier = modifier.fillMaxSize()
    ) {
        val currentCategory = category ?: return@AnimatedVisibility
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 24.dp)
                .testTag("liquid_glass_folder_popup")
        ) {
            val shape = RoundedCornerShape(32.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .heightIn(max = 480.dp)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xD8162036), Color(0xE80C1222))
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
                        shape = shape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = when (currentCategory) {
                                AppCategory.AI -> "Aplikasi AI"
                                AppCategory.GAMES -> "Koleksi Game"
                                AppCategory.ESSENTIALS -> "Aplikasi Penting"
                                else -> currentCategory.label
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${apps.size} aplikasi terdeteksi di Poco F7",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.68f)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onDismiss)
                            .testTag("close_folder_popup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close_panel),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (apps.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 26.dp)
                    ) {
                        Text(
                            text = "Belum ada aplikasi ${currentCategory.label} yang terpasang.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(LuminaCyan.copy(alpha = 0.22f))
                                .border(0.8.dp, LuminaCyan, RoundedCornerShape(16.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenAllDrawer()
                                }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "Lihat Semua Aplikasi",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(apps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                            AppIcon3DItem(
                                app = app,
                                iconStyle = iconStyle,
                                useCustom3DGlyphs = useCustom3DGlyphs,
                                showLabel = true,
                                iconSize = 56.dp,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onLongPressApp(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive iPhone 18 Pro Max Gesture Home Indicator Pill replacing 3-button nav.
 * Supports: Swipe Up (App Drawer / Back), Swipe Left/Right (Switch 3D Pages),
 * Tap (Return Home), Long Press (Quick Search).
 */
@Composable
fun GestureNavigationPill(
    onTapHome: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onLongPressSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isEngaged by remember { mutableStateOf(false) }
    var accumX by remember { mutableFloatStateOf(0f) }
    var accumY by remember { mutableFloatStateOf(0f) }

    val pillWidth by animateDpAsState(
        targetValue = if (isEngaged) 156.dp else 134.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "gesture_pill_width"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (isEngaged) 0.98f else 0.76f,
        label = "gesture_pill_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTapHome()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPressSearch()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isEngaged = true
                        accumX = 0f
                        accumY = 0f
                    },
                    onDragCancel = {
                        isEngaged = false
                    },
                    onDragEnd = {
                        isEngaged = false
                        when {
                            accumY < -36f -> {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeUp()
                            }
                            accumX < -48f -> {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeLeft()
                            }
                            accumX > 48f -> {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeRight()
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumX += dragAmount.x
                        accumY += dragAmount.y
                    }
                )
            }
            .testTag("iphone_gesture_home_pill")
    ) {
        Box(
            modifier = Modifier
                .width(pillWidth)
                .height(5.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = pillAlpha * 0.75f),
                            Color.White.copy(alpha = pillAlpha),
                            LuminaCyan.copy(alpha = if (isEngaged) 0.95f else 0.45f),
                            Color.White.copy(alpha = pillAlpha * 0.75f)
                        )
                    )
                )
        )
    }
}
