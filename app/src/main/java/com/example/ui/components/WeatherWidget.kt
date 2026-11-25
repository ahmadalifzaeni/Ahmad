package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PresetCities
import com.example.data.WeatherData
import com.example.data.WeatherScene
import com.example.data.WeatherUiState
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveWeatherWidget(
    weatherState: WeatherUiState,
    selectedCityId: String,
    useGpsWeather: Boolean,
    onRefresh: () -> Unit,
    onSelectCity: (String) -> Unit,
    onRequestGpsLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val activeScene = (weatherState as? WeatherUiState.Success)?.data?.scene ?: WeatherScene.PARTLY_CLOUDY
    val cardGradient = when (activeScene) {
        WeatherScene.CLEAR_DAY -> Brush.linearGradient(
            listOf(Color(0x521E6BA8), Color(0x463B82C4), Color(0x36F59E0B))
        )
        WeatherScene.CLEAR_NIGHT -> Brush.linearGradient(
            listOf(Color(0x66121A3A), Color(0x52222A5A), Color(0x44362C6E))
        )
        WeatherScene.RAIN, WeatherScene.STORM -> Brush.linearGradient(
            listOf(Color(0x6618293E), Color(0x581E3A54), Color(0x440F172A))
        )
        else -> Brush.linearGradient(
            listOf(Color(0x5221324B), Color(0x42354B6B), Color(0x3819263B))
        )
    }

    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardGradient)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.44f),
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.24f)
                    )
                ),
                shape = shape
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 18.dp, vertical = 15.dp)
            .testTag("interactive_weather_widget")
    ) {
        AnimatedContent(
            targetState = weatherState,
            transitionSpec = {
                fadeIn(tween(260)) togetherWith fadeOut(tween(180))
            },
            label = "weather_state_transition"
        ) { state ->
            when (state) {
                is WeatherUiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = LuminaCyan,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "Memuat cuaca langsung...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Sinkronisasi data atmosfer real-time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                is WeatherUiState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.fallbackCity,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Ketuk muat ulang untuk memperbarui cuaca",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("weather_retry_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.cd_refresh_weather),
                                tint = LuminaCyan
                            )
                        }
                    }
                }

                is WeatherUiState.Success -> {
                    WeatherSuccessContent(
                        data = state.data,
                        isExpanded = isExpanded,
                        selectedCityId = selectedCityId,
                        useGpsWeather = useGpsWeather,
                        onToggleExpand = { isExpanded = !isExpanded },
                        onRefresh = onRefresh,
                        onSelectCity = onSelectCity,
                        onRequestGpsLocation = onRequestGpsLocation
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherSuccessContent(
    data: WeatherData,
    isExpanded: Boolean,
    selectedCityId: String,
    useGpsWeather: Boolean,
    onToggleExpand: () -> Unit,
    onRefresh: () -> Unit,
    onSelectCity: (String) -> Unit,
    onRequestGpsLocation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Hero Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                AnimatedWeatherSceneCanvas(
                    scene = data.scene,
                    modifier = Modifier.size(56.dp)
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (useGpsWeather) Icons.Rounded.MyLocation else Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = LuminaCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = data.cityName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Text(
                        text = data.conditionLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                    Text(
                        text = "T:${data.highTempC}°  R:${data.lowTempC}°  •  ${data.updatedAtLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.62f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${data.temperatureC}°",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("weather_expand_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Compact atmospheric telemetry pills
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            WeatherMiniStatPill(
                icon = Icons.Rounded.WaterDrop,
                label = "${data.humidityPercent}%",
                tint = LuminaCyan
            )
            WeatherMiniStatPill(
                icon = Icons.Rounded.Air,
                label = "${data.windKmh} km/j",
                tint = Color(0xFF93C5FD)
            )
            WeatherMiniStatPill(
                icon = Icons.Rounded.WbSunny,
                label = "Terasa ${data.feelsLikeC}°",
                tint = LuminaAmber
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onRefresh)
                    .testTag("weather_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh_weather),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Smoothly animated expanded interactive forecast + city selector
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(240)) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(200))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.14f), thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Hourly forecast strip
                Text(
                    text = "Prakiraan Per Jam",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.84f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    data.hourly.forEach { hour ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(
                                    0.6.dp,
                                    Color.White.copy(alpha = 0.16f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 11.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = hour.hourLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Icon(
                                imageVector = sceneToSmallIcon(hour.scene),
                                contentDescription = null,
                                tint = sceneToAccentColor(hour.scene),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${hour.temperatureC}°",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }

                // 5-Day forecast
                if (data.daily.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Prakiraan 5 Hari",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.84f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        data.daily.forEach { day ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(68.dp)
                                )
                                Icon(
                                    imageVector = sceneToSmallIcon(day.scene),
                                    contentDescription = null,
                                    tint = sceneToAccentColor(day.scene),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${day.rainChancePercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LuminaCyan.copy(alpha = 0.85f),
                                    modifier = Modifier.width(38.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${day.minTempC}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.62f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${day.maxTempC}°",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Quick City Selector + GPS
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    CityGlassChip(
                        label = "GPS Otomatis",
                        selected = useGpsWeather,
                        onClick = onRequestGpsLocation,
                        tag = "weather_city_gps"
                    )
                    PresetCities.all.forEach { city ->
                        CityGlassChip(
                            label = city.name,
                            selected = !useGpsWeather && selectedCityId == city.id,
                            onClick = { onSelectCity(city.id) },
                            tag = "weather_city_${city.id}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMiniStatPill(
    icon: ImageVector,
    label: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.11f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.90f)
        )
    }
}

@Composable
private fun CityGlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val bg = if (selected) LuminaCyan.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.10f)
    val border = if (selected) LuminaCyan.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.18f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(0.8.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun AnimatedWeatherSceneCanvas(
    scene: WeatherScene,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "weather_canvas_anim")
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_spin"
    )
    val drift by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )
    val rainProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_fall"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (scene) {
            WeatherScene.CLEAR_DAY -> {
                val center = Offset(w * 0.5f, h * 0.5f)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x88FFD54F), Color.Transparent),
                        center = center,
                        radius = w * 0.50f
                    ),
                    radius = w * 0.50f,
                    center = center
                )
                for (i in 0 until 8) {
                    val angle = ((spin + i * 45f) * PI / 180.0).toFloat()
                    val inner = Offset(
                        center.x + cos(angle) * w * 0.27f,
                        center.y + sin(angle) * h * 0.27f
                    )
                    val outer = Offset(
                        center.x + cos(angle) * w * 0.39f,
                        center.y + sin(angle) * h * 0.39f
                    )
                    drawLine(
                        color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                        start = inner,
                        end = outer,
                        strokeWidth = w * 0.045f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFF176), Color(0xFFFF9800))),
                    radius = w * 0.21f,
                    center = center
                )
            }

            WeatherScene.CLEAR_NIGHT -> {
                val moonCenter = Offset(w * 0.52f, h * 0.48f)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x6693C5FD), Color.Transparent),
                        center = moonCenter,
                        radius = w * 0.44f
                    ),
                    radius = w * 0.44f,
                    center = moonCenter
                )
                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFF93C5FD))),
                    radius = w * 0.22f,
                    center = moonCenter
                )
                // Twinkling stars
                val starAlpha = 0.45f + 0.50f * ((drift + 1f) / 2f)
                drawCircle(Color.White.copy(alpha = starAlpha), radius = w * 0.035f, center = Offset(w * 0.22f, h * 0.28f))
                drawCircle(Color.White.copy(alpha = 1f - starAlpha * 0.5f), radius = w * 0.025f, center = Offset(w * 0.80f, h * 0.26f))
                drawCircle(Color(0xFF7DD3FC).copy(alpha = starAlpha), radius = w * 0.03f, center = Offset(w * 0.28f, h * 0.74f))
            }

            WeatherScene.PARTLY_CLOUDY -> {
                val sunCenter = Offset(w * 0.62f, h * 0.34f)
                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFFFA726))),
                    radius = w * 0.19f,
                    center = sunCenter
                )
                val shift = drift * w * 0.05f
                drawCircle(Color.White.copy(alpha = 0.92f), radius = w * 0.18f, center = Offset(w * 0.34f + shift, h * 0.58f))
                drawCircle(Color.White.copy(alpha = 0.95f), radius = w * 0.21f, center = Offset(w * 0.52f + shift, h * 0.55f))
                drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.90f), radius = w * 0.16f, center = Offset(w * 0.68f + shift, h * 0.61f))
            }

            WeatherScene.CLOUDY, WeatherScene.FOG -> {
                val shift = drift * w * 0.06f
                drawCircle(Color(0xFFCBD5E1).copy(alpha = 0.65f), radius = w * 0.18f, center = Offset(w * 0.62f - shift, h * 0.40f))
                drawCircle(Color.White.copy(alpha = 0.90f), radius = w * 0.20f, center = Offset(w * 0.36f + shift, h * 0.56f))
                drawCircle(Color.White.copy(alpha = 0.95f), radius = w * 0.22f, center = Offset(w * 0.54f + shift, h * 0.53f))
                drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.90f), radius = w * 0.16f, center = Offset(w * 0.70f + shift, h * 0.59f))
            }

            WeatherScene.RAIN, WeatherScene.STORM -> {
                val shift = drift * w * 0.04f
                val cloudTint = if (scene == WeatherScene.STORM) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
                drawCircle(cloudTint.copy(alpha = 0.92f), radius = w * 0.18f, center = Offset(w * 0.35f + shift, h * 0.42f))
                drawCircle(Color.White.copy(alpha = 0.92f), radius = w * 0.21f, center = Offset(w * 0.53f + shift, h * 0.38f))
                drawCircle(cloudTint.copy(alpha = 0.88f), radius = w * 0.16f, center = Offset(w * 0.69f + shift, h * 0.45f))

                // Falling rain streaks
                val drops = listOf(0.32f, 0.48f, 0.64f)
                drops.forEachIndexed { i, xFrac ->
                    val phase = (rainProgress + i * 0.33f) % 1f
                    val startY = h * (0.58f + phase * 0.24f)
                    drawLine(
                        color = LuminaCyan.copy(alpha = 1f - phase * 0.6f),
                        start = Offset(w * xFrac, startY),
                        end = Offset(w * (xFrac - 0.04f), startY + h * 0.13f),
                        strokeWidth = w * 0.045f,
                        cap = StrokeCap.Round
                    )
                }
                if (scene == WeatherScene.STORM && rainProgress > 0.72f) {
                    drawLine(
                        color = Color(0xFFFDE047),
                        start = Offset(w * 0.54f, h * 0.54f),
                        end = Offset(w * 0.46f, h * 0.76f),
                        strokeWidth = w * 0.055f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun sceneToSmallIcon(scene: WeatherScene): ImageVector = when (scene) {
    WeatherScene.CLEAR_DAY -> Icons.Rounded.WbSunny
    WeatherScene.CLEAR_NIGHT -> Icons.Rounded.NightsStay
    WeatherScene.PARTLY_CLOUDY -> Icons.Rounded.WbCloudy
    WeatherScene.CLOUDY, WeatherScene.FOG -> Icons.Rounded.Cloud
    WeatherScene.RAIN -> Icons.Rounded.WaterDrop
    WeatherScene.STORM -> Icons.Rounded.Thunderstorm
}

private fun sceneToAccentColor(scene: WeatherScene): Color = when (scene) {
    WeatherScene.CLEAR_DAY -> LuminaAmber
    WeatherScene.CLEAR_NIGHT -> Color(0xFF93C5FD)
    WeatherScene.PARTLY_CLOUDY -> Color(0xFFE2E8F0)
    WeatherScene.CLOUDY, WeatherScene.FOG -> Color(0xFFCBD5E1)
    WeatherScene.RAIN -> LuminaCyan
    WeatherScene.STORM -> Color(0xFFFDE047)
}
