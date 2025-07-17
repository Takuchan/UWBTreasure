package com.takuchan.uwbviaserial.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme
import com.takuchan.uwbviaserial.MainActivityUiState
import com.takuchan.uwbviaserial.UwbCoordinate
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor
import kotlin.math.*


@Composable
fun GameRoomView(
    uiState: MainActivityUiState,
    onAnchorPositionUpdate: (Int, Double, Double) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // アニメーション用の状態
    val infiniteTransition = rememberInfiniteTransition(label = "treasure_animation")

    // ソナーアニメーション
    val sonarRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_radius"
    )

    val sonarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_alpha"
    )

    // パルスアニメーション（より強い接近時）
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // キラキラエフェクト用
    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )

    // レーダー掃引エフェクト
    val radarSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                drawGameRoom(
                    uiState = uiState,
                    sonarRadius = sonarRadius,
                    sonarAlpha = sonarAlpha,
                    pulseScale = pulseScale,
                    sparkleRotation = sparkleRotation,
                    radarSweep = radarSweep
                )
            }

            // レジェンド
            GameLegend(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            // 接近時のメッセージ（アニメーション付き）
            // null安全にアクセス
            when (uiState.proximityVibrationAnchorId) {
                1 -> {
                    Text(
                        text = "😯 何かを感じる...",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .graphicsLayer {
                                scaleX = 1f + sin(sparkleRotation * PI / 180).toFloat() * 0.1f
                                scaleY = 1f + sin(sparkleRotation * PI / 180).toFloat() * 0.1f
                            },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                2 -> {
                    Text(
                        text = "✨ 温かくなってきた！",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .graphicsLayer {
                                scaleX = pulseScale * 0.8f
                                scaleY = pulseScale * 0.8f
                            },
                        color = Color(0xFFFF6B35),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                3 -> {
                    Text(
                        text = "🔥 すごく近い！！",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                rotationZ = sin(sparkleRotation * PI / 180).toFloat() * 5f
                            },
                        color = Color(0xFFFF3030),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                null -> {
                    // nullの場合は何も表示しない、または探索中メッセージを表示
                    // 必要に応じてコメントアウトを外してください
                    /*
                    Text(
                        text = "🔍 宝を探索中...",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    */
                }
                else -> {
                    // その他の値（0など）の場合も何も表示しない
                }
            }
        }
    }
}

private fun DrawScope.drawGameRoom(
    uiState: MainActivityUiState,
    sonarRadius: Float,
    sonarAlpha: Float,
    pulseScale: Float,
    sparkleRotation: Float,
    radarSweep: Float
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val padding = 40.dp.toPx()

    // 描画エリアの計算
    val availableWidth = canvasWidth - 2 * padding
    val availableHeight = canvasHeight - 2 * padding

    // スケールの計算
    val scaleX = availableWidth / uiState.roomWidth.toFloat()
    val scaleY = availableHeight / uiState.roomHeight.toFloat()
    val scale = minOf(scaleX, scaleY)

    // 中央に配置するためのオフセット
    val roomPixelWidth = uiState.roomWidth.toFloat() * scale
    val roomPixelHeight = uiState.roomHeight.toFloat() * scale
    val startX = padding + (availableWidth - roomPixelWidth) / 2
    val startY = padding + (availableHeight - roomPixelHeight) / 2

    // 部屋の境界線を描画
    drawRect(
        color = ComponentsColor.Primary,
        topLeft = Offset(startX, startY),
        size = androidx.compose.ui.geometry.Size(roomPixelWidth, roomPixelHeight),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
    )

    // 座標変換関数
    fun coordToPixel(x: Double, y: Double): Offset {
        return Offset(
            startX + (x * scale).toFloat(),
            startY + roomPixelHeight - (y * scale).toFloat() // Y軸を反転
        )
    }

    // アンカー間の線を描画
    drawAnchorConnections(uiState, ::coordToPixel)

    // 宝探しエフェクトを描画（アンカーより手前に）
    drawTreasureHuntEffects(
        uiState = uiState,
        coordToPixel = ::coordToPixel,
        sonarRadius = sonarRadius,
        sonarAlpha = sonarAlpha,
        pulseScale = pulseScale,
        sparkleRotation = sparkleRotation,
        radarSweep = radarSweep
    )

    // アンカーを描画
    drawAnchor(uiState.anchor0, ComponentsColor.Anchor0, "0", ::coordToPixel)
    drawAnchor(uiState.anchor1, ComponentsColor.Anchor1, "1", ::coordToPixel)
    drawAnchor(uiState.anchor2, ComponentsColor.Anchor2, "2", ::coordToPixel)

    // タグを描画（エフェクト付き）
    drawTag(uiState.tag, ::coordToPixel, uiState.proximityVibrationAnchorId, pulseScale)
}

private fun DrawScope.drawTreasureHuntEffects(
    uiState: MainActivityUiState,
    coordToPixel: (Double, Double) -> Offset,
    sonarRadius: Float,
    sonarAlpha: Float,
    pulseScale: Float,
    sparkleRotation: Float,
    radarSweep: Float
) {
    // null安全にアクセス
    val proximityLevel = uiState.proximityVibrationAnchorId
    if (proximityLevel == null || proximityLevel == 0) return

    val tagPosition = coordToPixel(uiState.tag.x, uiState.tag.y)
    val intensity = proximityLevel.toFloat()

    // ソナーエフェクト（レベルに応じて強度調整）
    val adjustedRadius = sonarRadius * intensity * 0.5f
    val adjustedAlpha = sonarAlpha * (intensity / 3f)

    // 複数のソナーリングを描画
    for (i in 0..2) {
        val ringRadius = adjustedRadius - (i * 20f)
        if (ringRadius > 0) {
            drawCircle(
                color = ComponentsColor.Tag.copy(alpha = adjustedAlpha * (1f - i * 0.3f)),
                radius = ringRadius,
                center = tagPosition,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }
    }

    // レーダー掃引エフェクト（レベル2以上）
    if (intensity >= 2f) {
        val sweepAngle = radarSweep * PI / 180
        val sweepLength = 80f * intensity
        val sweepEnd = Offset(
            tagPosition.x + cos(sweepAngle).toFloat() * sweepLength,
            tagPosition.y + sin(sweepAngle).toFloat() * sweepLength
        )

        drawLine(
            color = Color.Cyan.copy(alpha = 0.6f),
            start = tagPosition,
            end = sweepEnd,
            strokeWidth = 4.dp.toPx()
        )
    }

    // キラキラエフェクト（レベル3）
    if (intensity >= 3f) {
        val sparkleDistance = 40f
        for (i in 0..7) {
            val angle = (sparkleRotation + i * 45f) * PI / 180
            val sparklePos = Offset(
                tagPosition.x + cos(angle).toFloat() * sparkleDistance,
                tagPosition.y + sin(angle).toFloat() * sparkleDistance
            )

            drawCircle(
                color = Color.Yellow.copy(alpha = 0.8f),
                radius = 4.dp.toPx() * pulseScale,
                center = sparklePos
            )
        }
    }

    // 熱波エフェクト（レベル2以上）
    if (intensity >= 2f) {
        for (i in 0..3) {
            val waveRadius = 30f + i * 15f
            val waveAlpha = (0.3f - i * 0.07f) * intensity / 3f
            drawCircle(
                color = Color.Red.copy(alpha = waveAlpha),
                radius = waveRadius * pulseScale,
                center = tagPosition,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawAnchorConnections(
    uiState: MainActivityUiState,
    coordToPixel: (Double, Double) -> Offset
) {
    val connectionColor = Color.Gray.copy(alpha = 0.1f)
    val strokeWidth = 2.dp.toPx()

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor0.x, uiState.anchor0.y),
        end = coordToPixel(uiState.anchor1.x, uiState.anchor1.y),
        strokeWidth = strokeWidth
    )

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor0.x, uiState.anchor0.y),
        end = coordToPixel(uiState.anchor2.x, uiState.anchor2.y),
        strokeWidth = strokeWidth
    )

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor1.x, uiState.anchor1.y),
        end = coordToPixel(uiState.anchor2.x, uiState.anchor2.y),
        strokeWidth = strokeWidth
    )
}

fun DrawScope.drawAnchor(
    anchor: UwbCoordinate,
    color: Color,
    label: String,
    coordToPixel: (Double, Double) -> Offset
) {
    val center = coordToPixel(anchor.x, anchor.y)
    val radius = 16.dp.toPx()

    // 外側の円
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )

    // 内側の円
    drawCircle(
        color = Color.White,
        radius = radius * 0.6f,
        center = center
    )
}

fun DrawScope.drawTag(
    tag: UwbCoordinate,
    coordToPixel: (Double, Double) -> Offset,
    proximityLevel: Int?, // null許容型に変更
    pulseScale: Float
) {
    Log.d("tagLoc", "$tag")
    val center = coordToPixel(tag.x, tag.y)
    val baseRadius = 12.dp.toPx()

    // null安全にアクセスし、nullまたは0の場合は通常のサイズ
    val radius = if (proximityLevel != null && proximityLevel > 0) {
        baseRadius * pulseScale
    } else {
        baseRadius
    }

    // 接近レベルに応じたグロー効果（null安全）
    val glowIntensity = when (proximityLevel) {
        1 -> 1.2f
        2 -> 1.5f
        3 -> 2.0f
        else -> 1.0f // null含む
    }

    // 外側の円（グロー効果）
    drawCircle(
        color = ComponentsColor.Tag.copy(alpha = 0.3f * glowIntensity),
        radius = radius * 1.5f * glowIntensity,
        center = center
    )

    // メインの円
    drawCircle(
        color = ComponentsColor.Tag,
        radius = radius,
        center = center
    )

    // 内側の円
    drawCircle(
        color = Color.White,
        radius = radius * 0.5f,
        center = center
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewGameRoomView(){
    UWBviaSerialTheme {
        GameRoomView(
            uiState = MainActivityUiState(),
            onAnchorPositionUpdate = {it,it2,it3->

            }
        )
    }
}