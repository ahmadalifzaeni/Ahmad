package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.IconStyle3D
import com.example.data.InstalledApp

data class Custom3DGlyphSpec(
    val icon: ImageVector,
    val topColor: Color,
    val bottomColor: Color,
    val glowColor: Color
)

fun resolveCustom3DGlyph(app: InstalledApp): Custom3DGlyphSpec? {
    val text = "${app.packageName.lowercase()} ${app.label.lowercase()}"
    return when {
        listOf("dialer", "phone", "telepon").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Call, Color(0xFF42F2A8), Color(0xFF0E9F6E), Color(0xFF34D399)
        )
        listOf("mms", "sms", "messaging", "pesan", "whatsapp", "telegram", "chat").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.ChatBubble, Color(0xFF5AE8FF), Color(0xFF1D7AFC), Color(0xFF38BDF8)
        )
        listOf("camera", "kamera").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.CameraAlt, Color(0xFF6B7792), Color(0xFF222A3B), Color(0xFF94A3B8)
        )
        listOf("gallery", "galeri", "photos", "foto").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.PhotoLibrary, Color(0xFFFF9671), Color(0xFFFF4D8D), Color(0xFFFB7185)
        )
        listOf("chrome", "browser", "firefox", "opera", "edge").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Language, Color(0xFF64B5FF), Color(0xFF3F51F5), Color(0xFF60A5FA)
        )
        listOf("settings", "pengaturan", "setelan").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Settings, Color(0xFF8C9AB8), Color(0xFF354057), Color(0xFF94A3B8)
        )
        listOf("clock", "deskclock", "jam", "alarm").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Schedule, Color(0xFF475569), Color(0xFF0F172A), Color(0xFF60A5FA)
        )
        listOf("calendar", "kalender").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.CalendarMonth, Color(0xFFFF7A8A), Color(0xFFD92D4E), Color(0xFFFB7185)
        )
        listOf("calculator", "kalkulator").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Calculate, Color(0xFFFFB75E), Color(0xFFED7125), Color(0xFFFBBF24)
        )
        listOf("maps", "peta", "navigation").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Map, Color(0xFF5EEAD4), Color(0xFF0284C7), Color(0xFF38BDF8)
        )
        listOf("music", "musik", "spotify", "audio").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.MusicNote, Color(0xFFFF6B9D), Color(0xFF8B5CF6), Color(0xFFC084FC)
        )
        listOf("youtube", "video", "netflix", "player").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.PlayCircle, Color(0xFFFF6666), Color(0xFFC81E3A), Color(0xFFF87171)
        )
        listOf("mail", "gmail", "email").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Email, Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFF60A5FA)
        )
        listOf("files", "filemanager", "berkas", "documentsui").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Folder, Color(0xFF67E8F9), Color(0xFF0284C7), Color(0xFF38BDF8)
        )
        listOf("vending", "play store", "market", "store").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.ShoppingBag, Color(0xFF5EEAD4), Color(0xFF2563EB), Color(0xFF38BDF8)
        )
        listOf("notes", "catatan", "keep", "memo").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Description, Color(0xFFFDE047), Color(0xFFEA8A0C), Color(0xFFFACC15)
        )
        listOf("contacts", "kontak").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.AccountCircle, Color(0xFFA78BFA), Color(0xFF5B21B6), Color(0xFFA78BFA)
        )
        listOf("recorder", "rekam").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.Mic, Color(0xFFFB7185), Color(0xFFBE123C), Color(0xFFFB7185)
        )
        listOf("weather", "cuaca").any { text.contains(it) } -> Custom3DGlyphSpec(
            Icons.Rounded.WbSunny, Color(0xFF7DD3FC), Color(0xFF2563EB), Color(0xFF38BDF8)
        )
        else -> null
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon3DItem(
    app: InstalledApp,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    showLabel: Boolean,
    iconSize: Dp = 58.dp,
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_press_scale"
    )
    val pressTiltX by animateFloatAsState(
        targetValue = if (isPressed) -10f else (tiltY * 4f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_tilt_x"
    )
    val pressTiltY by animateFloatAsState(
        targetValue = if (isPressed) 8f else (tiltX * 5f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_tilt_y"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .widthIn(min = 64.dp, max = 82.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                rotationX = pressTiltX
                rotationY = pressTiltY
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("app_icon_${app.packageName}")
    ) {
        Sculpted3DIconBadge(
            app = app,
            iconStyle = iconStyle,
            useCustom3DGlyphs = useCustom3DGlyphs,
            size = iconSize,
            specularShift = tiltX
        )

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.94f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun Sculpted3DIconBadge(
    app: InstalledApp,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    size: Dp,
    specularShift: Float = 0f,
    modifier: Modifier = Modifier
) {
    val customGlyph = remember(app.packageName, app.label, useCustom3DGlyphs) {
        if (useCustom3DGlyphs) resolveCustom3DGlyph(app) else null
    }
    val cornerRadius = (size.value * 0.30f).dp
    val shape = RoundedCornerShape(cornerRadius)

    val baseBackgroundBrush = when {
        customGlyph != null && iconStyle == IconStyle3D.OBSIDIAN_3D -> Brush.linearGradient(
            colors = listOf(Color(0xFF242D42), Color(0xFF0E1320)),
            start = Offset(0f, 0f),
            end = Offset(180f, 180f)
        )
        customGlyph != null && iconStyle == IconStyle3D.CLAY_3D -> Brush.linearGradient(
            colors = listOf(
                customGlyph.topColor.copy(alpha = 0.96f),
                customGlyph.bottomColor.copy(alpha = 0.98f)
            ),
            start = Offset(0f, 0f),
            end = Offset(180f, 180f)
        )
        customGlyph != null -> Brush.linearGradient(
            colors = listOf(
                customGlyph.topColor.copy(alpha = 0.86f),
                customGlyph.bottomColor.copy(alpha = 0.92f)
            ),
            start = Offset(0f, 0f),
            end = Offset(180f, 180f)
        )
        iconStyle == IconStyle3D.OBSIDIAN_3D -> Brush.linearGradient(
            colors = listOf(Color(0xE021283B), Color(0xEC0C101B))
        )
        iconStyle == IconStyle3D.CLAY_3D -> Brush.linearGradient(
            colors = listOf(Color(0xEAEEF3FA), Color(0xD8CAD5E8))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0x52FFFFFF), Color(0x24FFFFFF))
        )
    }

    val glowColor = customGlyph?.glowColor ?: when (iconStyle) {
        IconStyle3D.GLASS_3D -> Color(0xFF5CE1FF)
        IconStyle3D.CLAY_3D -> Color(0xFF94A3B8)
        IconStyle3D.OBSIDIAN_3D -> Color(0xFF4F8BFF)
    }

    val borderBrush = when (iconStyle) {
        IconStyle3D.GLASS_3D -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.72f),
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.36f)
            )
        )
        IconStyle3D.CLAY_3D -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.80f),
                Color.Black.copy(alpha = 0.22f)
            )
        )
        IconStyle3D.OBSIDIAN_3D -> Brush.linearGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.12f),
                Color.Black.copy(alpha = 0.45f)
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = glowColor.copy(alpha = 0.55f),
                spotColor = Color.Black.copy(alpha = 0.72f)
            )
            .clip(shape)
            .background(baseBackgroundBrush)
            .drawBehind {
                // Sculpted 3D top specular highlight dome + subtle lower rim depth
                val shiftPx = specularShift * this.size.width * 0.25f
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (iconStyle == IconStyle3D.OBSIDIAN_3D) 0.18f else 0.42f),
                            Color.Transparent
                        ),
                        center = Offset(this.size.width * 0.32f + shiftPx, this.size.height * 0.12f),
                        radius = this.size.width * 0.85f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.70f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.30f)
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            }
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        if (customGlyph != null) {
            // Drop shadow layer behind glyph for crisp 3D elevation
            Icon(
                imageVector = customGlyph.icon,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.28f),
                modifier = Modifier
                    .size(size * 0.50f)
                    .graphicsLayer {
                        translationY = 4f
                        translationX = 1.5f
                    }
            )
            Icon(
                imageVector = customGlyph.icon,
                contentDescription = app.label,
                tint = if (iconStyle == IconStyle3D.OBSIDIAN_3D) customGlyph.topColor else Color.White,
                modifier = Modifier.size(size * 0.50f)
            )
        } else if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (iconStyle == IconStyle3D.GLASS_3D) 7.dp else 8.dp)
                    .clip(RoundedCornerShape((size.value * 0.20f).dp))
            )
        } else {
            Text(
                text = app.label.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}
