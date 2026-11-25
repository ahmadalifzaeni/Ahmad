package com.example.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.WallpaperPreset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ParallaxWallpaperLayer(
    preset: WallpaperPreset,
    customWallpaperUri: String?,
    pagerScrollFraction: Float,
    tiltX: Float,
    tiltY: Float,
    parallaxIntensity: Float,
    ambientGlowEnabled: Boolean,
    isBackdropBlurred: Boolean,
    modifier: Modifier = Modifier
) {
    val smoothPageFraction by animateFloatAsState(
        targetValue = pagerScrollFraction.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wallpaper_page_parallax"
    )
    val smoothTiltX by animateFloatAsState(
        targetValue = tiltX * parallaxIntensity,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wallpaper_tilt_x"
    )
    val smoothTiltY by animateFloatAsState(
        targetValue = tiltY * parallaxIntensity,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wallpaper_tilt_y"
    )

    val blurRadius by animateFloatAsState(
        targetValue = if (isBackdropBlurred) 22f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "backdrop_blur_radius"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_wallpaper_flow")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.5f) {
        Modifier.blur(blurRadius.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        val parallaxModifier = Modifier
            .fillMaxSize()
            .then(blurModifier)
            .graphicsLayer {
                val scaleBase = 1.18f + (0.10f * parallaxIntensity)
                scaleX = scaleBase
                scaleY = scaleBase

                val maxScrollShiftPx = size.width * 0.085f * parallaxIntensity
                val pageShift = (0.5f - smoothPageFraction) * 2f * maxScrollShiftPx
                val gyroShiftX = smoothTiltX * size.width * 0.04f
                val gyroShiftY = smoothTiltY * size.height * 0.03f

                translationX = pageShift + gyroShiftX
                translationY = gyroShiftY
            }

        if (!customWallpaperUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(customWallpaperUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = parallaxModifier
            )
        } else {
            Crossfade(
                targetState = preset,
                animationSpec = tween(durationMillis = 480),
                label = "wallpaper_crossfade"
            ) { activePreset ->
                Image(
                    painter = painterResource(id = activePreset.drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = parallaxModifier
                )
            }
        }

        // Dynamic breathing atmospheric light sheen
        if (ambientGlowEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val orb1Center = Offset(
                    x = size.width * (0.28f + 0.14f * cos(ambientPhase) + smoothTiltX * 0.08f),
                    y = size.height * (0.22f + 0.08f * sin(ambientPhase) + smoothTiltY * 0.06f)
                )
                val orb2Center = Offset(
                    x = size.width * (0.76f - 0.14f * sin(ambientPhase * 0.8f) - smoothTiltX * 0.06f),
                    y = size.height * (0.68f + 0.09f * cos(ambientPhase * 0.7f))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            preset.accentColor.copy(alpha = if (preset.isLightBackdrop) 0.14f else 0.18f),
                            Color.Transparent
                        ),
                        center = orb1Center,
                        radius = size.width * 0.68f
                    ),
                    radius = size.width * 0.68f,
                    center = orb1Center
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7B6CFF).copy(alpha = if (preset.isLightBackdrop) 0.09f else 0.14f),
                            Color.Transparent
                        ),
                        center = orb2Center,
                        radius = size.width * 0.62f
                    ),
                    radius = size.width * 0.62f,
                    center = orb2Center
                )
            }
        }

        // Clean readability gradient veil for crisp minimalist legibility on top & bottom
        val topScrimAlpha = if (preset.isLightBackdrop && customWallpaperUri == null) 0.24f else 0.36f
        val bottomScrimAlpha = if (preset.isLightBackdrop && customWallpaperUri == null) 0.34f else 0.48f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = topScrimAlpha),
                        0.28f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = bottomScrimAlpha)
                    )
                )
        )

        if (isBackdropBlurred) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF060911).copy(
                            alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.42f else 0.72f
                        )
                    )
            )
        }
    }
}
