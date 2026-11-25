package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.IconStyle3D
import com.example.data.InstalledApp
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan

@Composable
fun Interactive3DAppOverlay(
    app: InstalledApp?,
    isPinnedToDock: Boolean,
    iconStyle: IconStyle3D,
    useCustom3DGlyphs: Boolean,
    deviceTiltX: Float,
    deviceTiltY: Float,
    onDismiss: () -> Unit,
    onOpen: (InstalledApp) -> Unit,
    onToggleDock: (InstalledApp) -> Unit,
    onAppInfo: (InstalledApp) -> Unit,
    onHideApp: (InstalledApp) -> Unit,
    onUninstall: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    if (app != null) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = app != null,
        enter = fadeIn() + scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier.fillMaxSize()
    ) {
        val currentApp = app ?: return@AnimatedVisibility
        var dragRotX by remember(currentApp.packageName) { mutableFloatStateOf(0f) }
        var dragRotY by remember(currentApp.packageName) { mutableFloatStateOf(0f) }

        val animatedRotX by animateFloatAsState(
            targetValue = (dragRotX + deviceTiltY * 14f).coerceIn(-26f, 26f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "held_3d_rot_x"
        )
        val animatedRotY by animateFloatAsState(
            targetValue = (dragRotY + deviceTiltX * 16f).coerceIn(-28f, 28f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "held_3d_rot_y"
        )
        val density = LocalDensity.current.density

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 28.dp)
                .testTag("interactive_3d_app_overlay")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                // Interactive 3D Floating Hero Icon (responds to finger drag & gyroscope!)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .pointerInput(currentApp.packageName) {
                            detectDragGestures(
                                onDragEnd = {
                                    dragRotX = 0f
                                    dragRotY = 0f
                                },
                                onDragCancel = {
                                    dragRotX = 0f
                                    dragRotY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragRotY = (dragRotY + dragAmount.x * 0.24f).coerceIn(-24f, 24f)
                                    dragRotX = (dragRotX - dragAmount.y * 0.24f).coerceIn(-22f, 22f)
                                }
                            )
                        }
                        .graphicsLayer {
                            rotationX = animatedRotX
                            rotationY = animatedRotY
                            cameraDistance = 14f * density
                        }
                ) {
                    Sculpted3DIconBadge(
                        app = currentApp,
                        iconStyle = iconStyle,
                        useCustom3DGlyphs = useCustom3DGlyphs,
                        size = 96.dp,
                        specularShift = animatedRotY / 24f
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentApp.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Geser ikon untuk rotasi 3D",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Sleek iPhone-style Frosted Glass Context Menu
                val menuShape = RoundedCornerShape(24.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(menuShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xDD1A2236),
                                    Color(0xE8101626)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.36f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            shape = menuShape
                        )
                        .padding(vertical = 6.dp)
                ) {
                    ContextActionRow(
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        label = "Buka Aplikasi",
                        tint = LuminaCyan,
                        tag = "menu_open_app",
                        onClick = { onOpen(currentApp) }
                    )
                    MenuGlassDivider()
                    ContextActionRow(
                        icon = Icons.Rounded.PushPin,
                        label = if (isPinnedToDock) "Lepas dari Dock" else "Sematkan ke Dock",
                        tint = Color.White,
                        tag = "menu_toggle_dock",
                        onClick = { onToggleDock(currentApp) }
                    )
                    MenuGlassDivider()
                    ContextActionRow(
                        icon = Icons.Rounded.Info,
                        label = "Info Aplikasi",
                        tint = Color.White,
                        tag = "menu_app_info",
                        onClick = { onAppInfo(currentApp) }
                    )
                    MenuGlassDivider()
                    ContextActionRow(
                        icon = Icons.Rounded.VisibilityOff,
                        label = "Sembunyikan dari Beranda",
                        tint = Color.White.copy(alpha = 0.90f),
                        tag = "menu_hide_app",
                        onClick = { onHideApp(currentApp) }
                    )
                    if (!currentApp.isSystemApp) {
                        MenuGlassDivider()
                        ContextActionRow(
                            icon = Icons.Rounded.DeleteOutline,
                            label = "Hapus Instalasi",
                            tint = LuminaCoral,
                            tag = "menu_uninstall_app",
                            onClick = { onUninstall(currentApp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = tint,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MenuGlassDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.10f),
        thickness = 0.6.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
